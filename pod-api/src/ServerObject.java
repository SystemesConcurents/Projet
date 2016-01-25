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
        System.out.print("> ServerObject.lock_read() ");
        System.out.println(state);
        assert(client != null);

        if(state == State.WLT) {

            assert(lockingClients.size() == 1);

            try {
                if(!lockingClients.getFirst().equals(client)) {
                    System.out.print("# Invalidons le rédacteur... ");
                    this.object = lockingClients.getFirst().reduce_lock(id);
                    System.out.println("[OK]");
                }
            }
            catch(RemoteException e) {

                throw new RuntimeException(e);
            }
        }

        state = State.RLT;

        if(!lockingClients.contains(client))
            lockingClients.add(client);
        System.out.print("< ServerObject.lock_read() ");
        System.out.println(state);
    }

    public synchronized void lock_write(Client_itf client) {
        System.out.print("> ServerObject.lock_write() ");
        System.out.println(state);
        assert(client != null);

        if(state == State.WLT) {

            assert(lockingClients.size() == 1);

            try {
                if(!lockingClients.getFirst().equals(client)) {
                    System.out.print("# Invalidons le rédacteur... ");
                    this.object = lockingClients.getFirst().invalidate_writer(id);
                    System.out.println("[OK]");
                }
            }
            catch(RemoteException e) {
                throw new RuntimeException(e);
            }

            lockingClients.clear();
        }
        else if(state == State.RLT) {
            System.out.print("Aie aie aie !");
            System.out.println(lockingClients.size());
            for(Client_itf readingClient : lockingClients) {

                try {
                    if(!readingClient.equals(client)) {
                        System.out.print("# Invalidons ce lecteur... ");
                        readingClient.invalidate_reader(id);
                        System.out.println("[OK]");
                    }
                }
                catch(RemoteException e) {

                    throw new RuntimeException(e);
                }
            }

            lockingClients.clear();
        }

        state = State.WLT;

        lockingClients.add(client);
        System.out.print("< ServerObject.lock_write() ");
        System.out.println(state);
    }
}
