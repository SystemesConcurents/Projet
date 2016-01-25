import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;
import java.lang.RuntimeException;

public class SharedObject implements Serializable, SharedObject_itf {

    private enum State {
        NL,
        RLC,
        WLC,
        RLT,
        WLT,
        RLT_WLC
    }

    private int id;
    public Object obj;
    private Object cpy;
    private State state;
    private ReentrantLock mutex;
    private Condition endLock;
    private Condition endUnlock;

    public SharedObject() {
        this.id = 0;
        this.obj = null;
        this.state = State.NL;
        mutex = new ReentrantLock();
        endLock = mutex.newCondition();
        endUnlock = mutex.newCondition();
    }

    public SharedObject(int id) {
        this.id = id;
        this.obj = null;
        this.state = State.NL;
        mutex = new ReentrantLock();
        endLock = mutex.newCondition();
        endUnlock = mutex.newCondition();
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private static Object clone(Object i) {
        Object o = null;
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try{
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(i);
            oos.flush();
            oos.close();
            bos.close();
            byte[] byteData = bos.toByteArray();

            ByteArrayInputStream bais = new ByteArrayInputStream(byteData);

            o = (Object)(new ObjectInputStream(bais).readObject());
        }
        catch(ClassNotFoundException e) {
            throw new RuntimeException("ClassNotFoundException in SharedObject.saveCopy().");
        }
        catch(IOException e) {
            throw new RuntimeException("IOException in SharedObject.saveCopy().");
        }

        return o;
    }
    
    public void saveCopy() {
        cpy = SharedObject.clone(obj);        
    }

    public void restoreCopy() {
        obj = SharedObject.clone(obj);
    }

    // Invoked by the user program
    public void lock_read() {
        //System.out.print("> SharedObject.lock_read() ");
        //System.out.println(state);
        mutex.lock();
        assert(state != State.RLT);
        assert(state != State.WLT);
        assert(state != State.RLT_WLC);

        switch (state) {
            case NL:
                // mutex.unlock();
                obj = Client.lock_read(id);
                // mutex.lock();
            case RLC:
                state = State.RLT;
                break;
            case WLC:
                state = State.RLT_WLC;
                break;
            default:
                System.out.println("ILR : ça sert à rien tu l'as déjà !");
        }

        //System.out.print("< SharedObject.lock_read() ");
        //System.out.println(state);
        endLock.signal();
        mutex.unlock();
    }
    
    // Invoked by the user program
    public void lock_write() {
        //System.out.print("> SharedObject.lock_write() ");
        //System.out.println(state);
        mutex.lock();
        assert(state != State.RLT);
        assert(state != State.WLT);
        assert(state != State.RLT_WLC);

        switch(state) {
            case NL:
            case RLC:
                // Rendre le mutex au cas où on est bloqué par un autre client
                // demandant une écriture et qu'il faudrait nous invalider la
                // lecture
                mutex.unlock();
                obj = Client.lock_write(id);
                mutex.lock();
                state = State.WLT;
                break;
            case WLC:
                state = State.WLT;
                break;
            default:
                System.out.println("ILW : ça sert à rien tu l'as déjà !");
        }

        Transaction t = Transaction.getCurrentTransaction();
        if(t != null && t.isActive()) {
            t.addRelatedObject(this);
            saveCopy();
        }

        //System.out.print("< SharedObject.lock_write() ");
        //System.out.println(state);
        endLock.signal();
        mutex.unlock();
    }

    public boolean canUnlock() {
        return state == State.RLT_WLC || state == State.WLT || state == State.RLT;
    }

    // Invoked by the user program
    public void unlock() {
        //System.out.print("> SharedObject.unlock() ");
        //System.out.println(state);
        mutex.lock();
        assert(state != State.NL);
        assert(state != State.RLC);
        assert(state != State.WLC);

        switch(state) {
            case RLT_WLC:
            case WLT:
                state = State.WLC;
                break;
            case RLT:
                state = State.RLC;
                break;
            default:
                System.out.println("IUL : arrête, tu l'as déjà plus !");
        }

        //System.out.print("< SharedObject.unlock() ");
        //System.out.println(state);
        endUnlock.signal();
        mutex.unlock();
   }


    // Invoked remotely by the server
    public Object reduce_lock() {
        //System.out.print("> SharedObject.reduce_lock() ");
        //System.out.println(state);
        mutex.lock();

        while(state == State.NL || state == State.RLC || state == State.RLT)
            endLock.awaitUninterruptibly();

        switch(state) {
            case RLT_WLC:
                state = State.RLT;
                break;
            case WLT:
                endUnlock.awaitUninterruptibly();
            case WLC:
                state = State.RLC;
                break;
            default:
                throw new RuntimeException("Invalid reduce_lock");
        }

        //System.out.print("< SharedObject.reduce_lock() ");
        //System.out.println(state);
        mutex.unlock();
        return obj;
    }

    // Invoked remotely by the server
    public void invalidate_reader() {
        //System.out.print("> SharedObject.invalidate_reader() ");
        //System.out.println(state);
        mutex.lock();

        while(state == State.NL)
            endLock.awaitUninterruptibly();

        switch(state) {
            case RLT:
                endUnlock.awaitUninterruptibly();
            case RLC:
                state = State.NL;
                break;
            default:
                throw new RuntimeException("Invalid invalidate_reader");
        }

        obj = null;
        //System.out.print("< SharedObject.invalidate_reader() ");
        //System.out.println(state);
        mutex.unlock();
    }

    // Invoked remotely by the server
    public Object invalidate_writer() {
        //System.out.print("> SharedObject.invalidate_writer() ");
        //System.out.println(state);
        mutex.lock();

        while(state == State.NL || state == State.RLC || state == State.RLT)
            endLock.awaitUninterruptibly();

        switch(state) {
            case RLT_WLC:
            case WLT:
                endUnlock.awaitUninterruptibly();
            case WLC:
                state = State.NL;
                break;
            default:
                throw new RuntimeException("Invalid invalidate_writer");
        }

        //System.out.print("< SharedObject.invalidate_writer() ");
        //System.out.println(state);
        mutex.unlock();
        return obj;
    }

}
