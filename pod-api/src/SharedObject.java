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
                throw new RuntimeException("Invalid lock_read");
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
                System.out.println("# essayons de prendre le vérou en écriture...");
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
                throw new RuntimeException("Invalid lock_write");
        }

        Transaction t = Transaction.getCurrentTransaction();
        if(t != null && t.isActive()) {
            t.addRelatedObject(this);
        }

        //System.out.print("< SharedObject.lock_write() ");
        //System.out.println(state);
        endLock.signal();
        mutex.unlock();
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
                throw new RuntimeException("Invalid unlock");
        }

        //System.out.print("< SharedObject.unlock() ");
        //System.out.println(state);
        endUnlock.signal();
        mutex.unlock();
   }


    // Invoked remotely by the server
    public Object reduce_lock() {
        System.out.print("> SharedObject.reduce_lock() ");
        System.out.println(state);
        mutex.lock();

        if(state == State.NL || state == State.RLC || state == State.RLT)
            endLock.awaitUninterruptibly();

        switch(state) {
            case RLT_WLC:
                state = State.RLT;
                break;
            case WLT:
                endUnlock.awaitUninterruptibly();
                // Case ?
            case WLC:
                state = State.RLC;
                break;
            default:
                throw new RuntimeException("Invalid reduce_lock");
        }

        System.out.print("< SharedObject.reduce_lock() ");
        System.out.println(state);
        mutex.unlock();
        return obj;
    }

    // Invoked remotely by the server
    public void invalidate_reader() {
        System.out.print("> SharedObject.invalidate_reader() ");
        System.out.println(state);
        mutex.lock();

        if(state == State.NL)
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
        System.out.print("< SharedObject.invalidate_reader() ");
        System.out.println(state);
        mutex.unlock();
    }

    // Invoked remotely by the server
    public Object invalidate_writer() {
        System.out.print("> SharedObject.invalidate_writer() ");
        System.out.println(state);
        mutex.lock();

        if(state == State.NL || state == State.RLC || state == State.RLT)
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

        System.out.print("< SharedObject.invalidate_writer() ");
        System.out.println(state);
        mutex.unlock();
        return obj;
    }

}
