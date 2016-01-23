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

    public Server() throws java.rmi.RemoteException {

        objectsByName = new HashMap<String, ServerObject>();
        objectsById = new HashMap<Integer, ServerObject>();
        currentId = 0;
    }

    public static void main(String args[]) throws Exception {

        try {
            System.out.println("Création du registre RMI");
            LocateRegistry.createRegistry(1337);
        }
        catch (RemoteException e) {
            System.out.println("Annulation : le registre existe déjà.");
        }

        System.out.println("Déclaration des objets partagés dans le registre");
        Server server = new Server();
    }

    public synchronized int lookup(String name) throws java.rmi.RemoteException {

        ServerObject object = objectsByName.get(name);

        if(object == null)
            return -1;

        return object.getId();
    }

    public synchronized void register(String name, int id) throws java.rmi.RemoteException {

        ServerObject serverObject = objectsById.get(id);
        if(serverObject != null)
            objectsByName.put(name, serverObject);

        return;
    }

    public synchronized int create(Object object) throws java.rmi.RemoteException {

        int id = currentId++;
        ServerObject serverObject = new ServerObject(id, object);
        objectsById.put(id, serverObject);

        return id;
    }

    public synchronized Object lock_read(int id, Client_itf client) throws java.rmi.RemoteException {

        ServerObject serverObject = objectsById.get(id);

        serverObject.lock_read(client);

        return serverObject.getObject();
    }


    public synchronized Object lock_write(int id, Client_itf client) throws java.rmi.RemoteException {

        ServerObject serverObject = objectsById.get(id);

        serverObject.lock_write(client);

        return serverObject.getObject();
    }
}
