import java.rmi.*;
import java.util.*;

public class ServerObject {

    private enum State {
        NL,
        RLT,
        WLT
    }

    private int id;
    private State state;
    private LinkedList<Client_itf> lockingClients;
    private Object object;

    public ServerObject(int id, Object object) {

        this.id = id;
        this.state = State.NL;
        this.lockingClients = new LinkedList<Client_itf>();
        this.object = object;
    }

    public int getId() {

        return id;
    }

    public Object getObject() {

        return this.object;
    }

    public synchronized void lock_read(Client_itf client) {

        assert(client != null);

        if(state == State.WLT) {

            assert(lockingClients.size() == 1);

            try {
                if(!lockingClients.getFirst().equals(client))
                    this.object = lockingClients.getFirst().reduce_lock(id);
            }
            catch(RemoteException e) {

                throw new RuntimeException(e);
            }
        }

        state = State.RLT;

        if(!lockingClients.contains(client))
            lockingClients.add(client);
    }

    public synchronized void lock_write(Client_itf client) {

        assert(client != null);

        if(state == State.WLT) {

            assert(lockingClients.size() == 1);

            try {
                if(!lockingClients.getFirst().equals(client))
                    this.object = lockingClients.getFirst().invalidate_writer(id);
            }
            catch(RemoteException e) {
                throw new RuntimeException(e);
            }

            lockingClients.clear();
        }
        else if(state == State.RLT) {

            for(Client_itf readingClient : lockingClients) {

                try {
                    if(!readingClient.equals(client))
                        client.invalidate_reader(id);
                }
                catch(RemoteException e) {

                    throw new RuntimeException(e);
                }
            }

            lockingClients.clear();
        }

        state = State.WLT;

        lockingClients.add(client);
    }
}
