import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.net.*;
import java.util.*;


public class Client extends UnicastRemoteObject implements Client_itf {

    public static Client_itf client;
    private static Server_itf server;
    private static HashMap<Integer,SharedObject> sharedObjects;


    public Client() throws RemoteException {
        super();
    }


///////////////////////////////////////////////////
//         Interface to be used by applications
///////////////////////////////////////////////////

    // Initialization of the client layer
    public static void init() {
        //System.out.println("> Client.init()");

        try {
            client = new Client();
        }
        catch (RemoteException e) {
            System.out.println("RemoteException in Client.init() (1).");
        }

        try {
            server = (Server_itf) Naming.lookup("//localhost:1337/Server");
        }
        catch(MalformedURLException e) {
            System.out.println("MalformedURLException in Client.init().");
        }
        catch(RemoteException e) {
            System.out.println("RemoteException in Client.init() (2).");
        }
        catch(NotBoundException e) {
            System.out.println("NotBoundException in Client.init().");
        }

        assert(server != null);
        sharedObjects = new HashMap<Integer, SharedObject>();
    }

    // Lookup in the naming server
    public static SharedObject lookup(String name) {
        //System.out.println("> Client.lookup()");

        int id = 0;
        try {
            id = server.lookup(name);
        }
        catch(RemoteException e) {
            System.out.println("RemoteException in Client.lookup().");
        }

        SharedObject s = null;
        if(id != -1) {
            s = sharedObjects.get(id);
            if(s == null) {
                s = new SharedObject(id);
                sharedObjects.put(id, s);
            }
        }

        return s;
    }

    // Binding in the naming server
    public static void register(String name, SharedObject_itf so) {
        //System.out.println("> Client.register()");

        SharedObject s = (SharedObject)so;
        int id = s.getId();
        try {
            server.register(name, id);
        }
        catch(RemoteException e) {
            System.out.println("RemoteException in Client.register().");
        }

        sharedObjects.put(id, s);
    }

    // Creation of a shared object
    public static SharedObject create(Object o) {
        //System.out.println("> Client.create()");

        int id=0;
        try {
            id = server.create(o);
        }
        catch(RemoteException e) {
            System.out.println("RemoteException in Client.create().");
        }

        SharedObject s = null;
        if (id != -1) {
            s = new SharedObject(id);
        }

        return s;
    }

/////////////////////////////////////////////////////////////
//    Interface to be used by the consistency protocol
////////////////////////////////////////////////////////////

    // Request a read lock from the server
    public static synchronized Object lock_read(int id) {
        //System.out.println("> Client.lock_read()");

        Object object = null;
        try {
            object= server.lock_read(id, client);
        }
        catch(RemoteException e) {
            System.out.println("RemoteException in Client.lock_read().");
        }

        //System.out.println("< Client.lock_read()");
        return object;
    }

    // Request a write lock from the server
    public static synchronized Object lock_write(int id) {
        //System.out.println("> Client.lock_write()");

        Object object = null;
        try {
            object = server.lock_write(id, client);
        }
        catch(RemoteException e) {
            System.out.println("RemoteException in Client.lock_read().");
        }

        //System.out.println("< Client.lock_write()");
        return object;
    }

    // Receive a lock reduction request from the server
    public synchronized Object reduce_lock(int id) throws java.rmi.RemoteException {
        System.out.println("> Client.reduce_lock()");

        SharedObject s = sharedObjects.get(id);

        System.out.println("< Client.reduce_lock()");
        return s.reduce_lock();
    }


    // Receive a reader invalidation request from the server
    public synchronized void invalidate_reader(int id) throws java.rmi.RemoteException {
        System.out.println("> Client.invalidate_reader()");

        SharedObject s = sharedObjects.get(id);

        s.invalidate_reader();
        System.out.println("< Client.invalidate_reader()");
    }


    // Receive a writer invalidation request from the server
    public synchronized Object invalidate_writer(int id) throws java.rmi.RemoteException {
        System.out.println("> Client.invalidate_writer()");

        SharedObject s = sharedObjects.get(id);

        Object o = s.invalidate_writer();
        System.out.println("< Client.invalidate_writer()");
        return o;
    }
}

