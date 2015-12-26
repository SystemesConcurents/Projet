import java.rmi.RemoteException;
import java.util.LinkedList;

public class ServerObject
{
    private enum State
    {
        RLT,
        WLT,
        NL
    };

    private int id;
    private State state;
    private LinkedList<Client_itf> client_locks;
    private Object obj;

    public ServerObject(int id, Object obj)
    {
        this.state = State.NL;
        this.client_locks = new LinkedList<Client_itf>();
        this.obj = obj;
        this.id = id;
    }

    public int getId()
    {
        return id;
    }

    public Object getObj()
    {
        return this.obj;
    }

    public synchronized void lock_read(Client_itf client)
    {
        Server.log.info(String.format("lock_read object %d [client=%d] [state=%s] [thread=%d]", id, client.hashCode(), this.state.name(), Thread.currentThread().getId()));
        assert(client != null);

        if(state == State.WLT)
        {
            assert(client_locks.size() == 1);
            try
            {
                if(!client_locks.getFirst().equals(client))
                {
                    Server.log.info(String.format("reduce_lock object %d [client=%d] [state=%s] [thread=%d]", id, client_locks.getFirst().hashCode(), this.state.name(), Thread.currentThread().getId()));
                    this.obj = client_locks.getFirst().reduce_lock(id);
                }
            }
            catch(RemoteException e) {
                throw new RuntimeException(e);
            }
        }

        state = State.RLT;

        if(!client_locks.contains(client))
            client_locks.add(client);

        Server.log.info(String.format("end of lock_read object %d [client=%d] [state=%s] [thread=%d]", id, client.hashCode(), this.state.name(), Thread.currentThread().getId()));
    }

    public synchronized void lock_write(Client_itf client)
    {
        Server.log.info(String.format("lock_write object %d [client=%d] [state=%s] [thread=%d]", id, client.hashCode(), this.state.name(), Thread.currentThread().getId()));
        assert(client != null);

        if(state == State.WLT)
        {
            assert(client_locks.size() == 1);
            try
            {
                if(!client_locks.getFirst().equals(client))
                {
                    Server.log.info(String.format("invalidate_writer object %d [client=%d] [state=%s] [thread=%d]", id, client_locks.getFirst().hashCode(), this.state.name(), Thread.currentThread().getId()));
                    this.obj = client_locks.getFirst().invalidate_writer(id);
                }
            }
            catch(RemoteException e) {
                throw new RuntimeException(e);
            }
        }
        else if(state == State.RLT)
        {
            for(Client_itf c: client_locks)
            {
                try
                {
                    if(!c.equals(client))
                    {
                        Server.log.info(String.format("invalidate_reader object %d [client=%d] [state=%s] [thread=%d]", id, c.hashCode(), this.state.name(), Thread.currentThread().getId()));
                        c.invalidate_reader(id);
                    }
                }
                catch(RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        client_locks.clear();
        client_locks.add(client);
        state = State.WLT;

        Server.log.info(String.format("end of lock_write object %d [client=%d] [state=%s] [thread=%d]", id, client.hashCode(), this.state.name(), Thread.currentThread().getId()));
    }
}
