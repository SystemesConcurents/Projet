import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class SharedObject implements Serializable, SharedObject_itf
{
    private static final long serialVersionUID = 8271196138090195418L;

    private ReentrantLock mutex;
    private Condition end_unlock;
    private Condition end_lock;

    private enum State {
        NL,
        RLC,
        WLC,
        RLT,
        WLT,
        RLT_WLC,
    };

    private int id;

    // public as it is directly acessed in the « irc » example.
    // don't ask me why...
    public Object obj;

    private State state;

    private void initialize()
    {
        this.mutex = new ReentrantLock();
        this.end_unlock = this.mutex.newCondition();
        this.end_lock = this.mutex.newCondition();

        this.state = State.NL;
        this.id = -1;
        this.obj = null;
    }

    public SharedObject(int id)
    {
        this.initialize();
        this.id = id;
    }

    public int getId()
    {
        return this.id;
    }

    public Object getObj()
    {
        return this.obj;
    }

    // invoked by the user program on the client node
    public void lock_read()
    {
        mutex.lock();
        Client.log.info(String.format("lock_read object %d [state=%s] [thread=%d]", this.id, this.state.name(), Thread.currentThread().getId()));
        assert(this.state == State.NL || this.state == State.RLC || this.state == State.WLC);

        Object o;
        if(this.state == State.NL)
        {
            mutex.unlock();
            o = Client.lock_read(this.id);
            mutex.lock();

            assert(this.state == State.NL);
            assert(o != null);
            this.obj = o;
            this.state = State.RLT;
        }
        else if(this.state == State.RLC)
        {
            this.state = State.RLT;
        }
        else if(this.state == State.WLC)
        {
            this.state = State.RLT_WLC;
        }

        end_lock.signal();
        Client.log.info(String.format("end of lock_read object %d [state=%s] [thread=%d]", this.id, this.state.name(), Thread.currentThread().getId()));
        mutex.unlock();
    }

    // invoked by the user program on the client node
    public void lock_write()
    {
        mutex.lock();
        Client.log.info(String.format("lock_write object %d [state=%s] [thread=%d]", this.id, this.state.name(), Thread.currentThread().getId()));
        assert(this.state != State.WLT);

        Object o;
        if(this.state == State.NL || this.state == State.RLC || this.state == State.RLT)
        {
            if(this.state == State.RLT) /* in case there is always an invalidate_reader() */
            {
                this.state = State.RLC;
                end_unlock.signal();
            }

            mutex.unlock();
            o = Client.lock_write(this.id);
            mutex.lock();

            assert(o != null);
            this.obj = o;
        }

        this.state = State.WLT;

        end_lock.signal();
        Client.log.info(String.format("end of lock_write object %d [state=%s] [thread=%d]", this.id, this.state.name(), Thread.currentThread().getId()));
        mutex.unlock();
    }

    // invoked by the user program on the client node
    public void unlock()
    {
        mutex.lock();
        Client.log.info(String.format("unlock object %d [state=%s] [thread=%d]", this.id, this.state.name(), Thread.currentThread().getId()));
        assert(this.state == State.RLT || this.state == State.WLT || this.state == State.RLT_WLC);

        if(this.state == State.RLT)
        {
            this.state = State.RLC;
        }
        else if(this.state == State.WLT || this.state == State.RLT_WLC)
        {
            this.state = State.WLC;
        }

        end_unlock.signal();
        Client.log.info(String.format("end of unlock object %d [state=%s] [thread=%d]", this.id, this.state.name(), Thread.currentThread().getId()));
        mutex.unlock();
    }

    // callback invoked remotely by the server
    public Object reduce_lock()
    {
        mutex.lock();
        Client.log.info(String.format("reduce_lock object %d [state=%s] [thread=%d]", this.id, this.state.name(), Thread.currentThread().getId()));

        while(this.state == State.NL || this.state == State.RLC || this.state == State.RLT) // hack needed in case there is a parallel lock_*
        {
            end_lock.awaitUninterruptibly();
        }

        while(this.state == State.WLT)
        {
            end_unlock.awaitUninterruptibly();
        }

        assert(this.state == State.WLC || this.state == State.RLT_WLC);
        assert(this.obj != null);

        if(this.state == State.WLC)
            this.state = State.RLC;
        else if(this.state == State.RLT_WLC)
            this.state = State.RLT;

        Object o = this.obj;

        Client.log.info(String.format("end of reduce_lock object %d [state=%s] [thread=%d]", this.id, this.state.name(), Thread.currentThread().getId()));
        mutex.unlock();
        return o;
    }

    // callback invoked remotely by the server
    public void invalidate_reader()
    {
        mutex.lock();
        Client.log.info(String.format("invalidate_reader object %d [state=%s] [thread=%d]", this.id, this.state.name(), Thread.currentThread().getId()));

        while(this.state == State.NL) // hack needed in case there is a parallel lock_*
        {
            end_lock.awaitUninterruptibly();
        }

        while(this.state == State.RLT)
        {
            end_unlock.awaitUninterruptibly();
        }

        assert(this.state == State.RLC);
        this.state = State.NL;
        this.obj = null; // to help debugging

        Client.log.info(String.format("end of invalidate_reader object %d [state=%s] [thread=%d]", this.id, this.state.name(), Thread.currentThread().getId()));
        mutex.unlock();
    }

    // callback invoked remotely by the server
    public Object invalidate_writer()
    {
        mutex.lock();
        Client.log.info(String.format("invalidate_writer object %d [state=%s] [thread=%d]", this.id, this.state.name(), Thread.currentThread().getId()));

        while(this.state == State.NL || this.state == State.RLC || this.state == State.RLT) // hack needed in case there is a parallel lock_*
        {
            end_lock.awaitUninterruptibly();
        }

        while(this.state == State.RLT_WLC || this.state == State.WLT)
        {
            end_unlock.awaitUninterruptibly();
        }

        assert(this.state == State.WLC);
        assert(this.obj != null);
        this.state = State.NL;
        Object o = this.obj;
        this.obj = null;

        Client.log.info(String.format("end of invalidate_writer object %d [state=%s] [thread=%d]", this.id, this.state.name(), Thread.currentThread().getId()));
        mutex.unlock();
        return o;
    }

    // Serialization

    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
        out.writeInt(this.id);
    }

    private void readObject(java.io.ObjectInputStream out) throws IOException, ClassNotFoundException
    {
        this.initialize();
        this.id = out.readInt();

        if(Client.client != null) // not on the server
        {
            Client.internal_register(this);
        }
    }
}
