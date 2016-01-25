import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;

public class Server extends UnicastRemoteObject implements Server_itf {

    private HashMap<String, ServerObject> objectsByName;
    private HashMap<Integer, ServerObject> objectsById;
    private int currentId;

    public Server() throws java.rmi.RemoteException {

        objectsByName = new HashMap<String, ServerObject>();
        objectsById = new HashMap<Integer, ServerObject>();
        currentId = 0;
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

    public synchronized int lookup(String name) throws java.rmi.RemoteException {
        System.out.println("> Server.lookup()");

        ServerObject object = objectsByName.get(name);

        if(object == null)
            return -1;

        return object.getId();
    }

    public synchronized void register(String name, int id) throws java.rmi.RemoteException {
        System.out.print("> Server.register() ");
        System.out.println(name);

        ServerObject serverObject = objectsById.get(id);
        if(serverObject != null)
            objectsByName.put(name, serverObject);

        System.out.print("< Server.register() ");
        System.out.println(name);
        return;
    }

    public synchronized int create(Object object) throws java.rmi.RemoteException {
        System.out.println("> Server.create()");

        int id = currentId++;
        ServerObject serverObject = new ServerObject(id, object);
        objectsById.put(id, serverObject);

        System.out.println("< Server.create()");
        return id;
    }

    public synchronized Object lock_read(int id, Client_itf client) throws java.rmi.RemoteException {
        //System.out.println("> Server.lock_read()");

        ServerObject serverObject = objectsById.get(id);

        serverObject.lock_read(client);

        //System.out.println("< Server.lock_read()");
        return serverObject.getObject();
    }


    public synchronized Object lock_write(int id, Client_itf client) throws java.rmi.RemoteException {
        //System.out.println("> Server.lock_write()");

        ServerObject serverObject = objectsById.get(id);

        serverObject.lock_write(client);

        //System.out.println("< Server.lock_write()");
        return serverObject.getObject();
    }
}
