import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.concurrent.locks.ReentrantLock;

public class Server extends UnicastRemoteObject implements Server_itf {

    private HashMap<String, ServerObject> objectsByName;
    private HashMap<Integer, ServerObject> objectsById;
    private int currentId;
    private ReentrantLock mutex;

    public Server() throws java.rmi.RemoteException {

        objectsByName = new HashMap<String, ServerObject>();
        objectsById = new HashMap<Integer, ServerObject>();
        currentId = 0;
        mutex = new ReentrantLock();
    }

    public static void main(String args[]) throws Exception {
        System.out.println("> Server.main()");

        try {
            LocateRegistry.createRegistry(1337);
        }
        catch (RemoteException e) {
            System.out.println("RemoteException in Server.main() : RMI registry already exists.");
        }

        Server server = new Server();
        Naming.rebind("//localhost:1337/Server", server);
    }

    public int lookup(String name) throws java.rmi.RemoteException {
        System.out.println("> Server.lookup()");

        mutex.lock();
        ServerObject object = objectsByName.get(name);
        mutex.unlock();

        if(object == null)
            return -1;

        return object.getId();
    }

    public void register(String name, int id) throws java.rmi.RemoteException {
        System.out.print("> Server.register() ");
        System.out.println(name);

        mutex.lock();
        ServerObject serverObject = objectsById.get(id);
        if(serverObject != null)
            objectsByName.put(name, serverObject);
        mutex.unlock();

        System.out.print("< Server.register() ");
        System.out.println(name);
        return;
    }

    public int create(Object object) throws java.rmi.RemoteException {
        System.out.println("> Server.create()");

        mutex.lock();
        int id = currentId++;
        ServerObject serverObject = new ServerObject(id, object);
        objectsById.put(id, serverObject);
        mutex.unlock();

        System.out.println("< Server.create()");
        return id;
    }

    public Object lock_read(int id, Client_itf client) throws java.rmi.RemoteException {
        System.out.println("> Server.lock_read()");

        mutex.lock();
        ServerObject serverObject = objectsById.get(id);

        serverObject.lock_read(client);
        mutex.unlock();

        System.out.println("< Server.lock_read()");
        return serverObject.getObject();
    }


    public Object lock_write(int id, Client_itf client) throws java.rmi.RemoteException {
        System.out.println("> Server.lock_write()");

        mutex.lock();
        ServerObject serverObject = objectsById.get(id);

        serverObject.lock_write(client);
        mutex.unlock();

        System.out.println("< Server.lock_write()");
        return serverObject.getObject();
    }
}
