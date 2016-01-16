import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.net.*;
import java.util.logging.Logger;

public class Server extends UnicastRemoteObject implements Server_itf {
    
    private ReentrantLock mutex;
    private HashMap<String, ServerObject> objectsByName;
    private HashMap<Integer, ServerObject> objectsById;
    private int currentId;

    public Server() {
        
        mutex = new ReentrantLock();
        objectsByName = new HashMap<String, ServerObject>();
        objectsById = new HashMap<Integer, ServerObject>();
        currentId = 0;
    }

    public static void main(String args[]) throws Exception {
        
        try {
            System.out.println("Création du registre RMI");
            LocaRegistry.createRegistry(1337);
        }
        catch (RemoteException e) {
            System.out.println("Annulation : le registre existe déjà.");
        }
        
        System.out.println("Déclaration des objets partagés dans le registre");
        Server server = new Server();
    }

    public int lookup(String name) throws java.rmi.RemoteException {
        
        mutex.lock();
        ServerObject object = objectsByName.get(name);
        mutex.unlock();
        
        if(object == null)
            return -1;

        return object.getId();
    }

    public void register(String name, int id) throws java.rmi.RemoteException {
        
        mutex.lock();
        ServerObject serverObject = objectsById.get(id);
        if(object != null)
            objectsByName.put(name, serverObject);
        mutex.unlock();

        return;
    }

    public int create(Object object) throws java.rmi.RemoteException {
        
        mutex.lock();
        int id = currentId++;
        ServerObject serverObject = new ServerObject(id, object);
        objectsById.put(id, serverObject);
        mutex.unlock();
        
        return id;
    }

    public Object lock_read(int id, Client_itf client) throws java.rmi.RemoteException {
        
        mutex.lock();
        ServerObject serverObject = objectsById.get(id);
        mutex.unlock();

        serverObject.lock_read(client);

        return serverObject.getObject();
    }


    public Object lock_write(int id, Client_itf client) throws java.rmi.RemoteException {
        
        mutex.lock();
        ServerObject serverObject = objectsById.get(id);
        mutex.unlock();

        serverObject.lock_write(client);

        return serverObject.getObject();
    }
}
