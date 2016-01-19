import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.net.*;
import java.util.* ;
import java.util.concurrent.locks.ReentrantLock;


public class Client extends UnicastRemoteObject implements Client_itf {

	public static Client client ;
    private static ReentrantLock mutex = new ReentrantLock() ;
	private static Server serveur ;
	private static HashMap<Integer,SharedObject> sharedObjects ; 
	

	public Client() throws RemoteException {
		super();
	}


///////////////////////////////////////////////////
//         Interface to be used by applications
///////////////////////////////////////////////////

	// initialization of the client layer
	public static void init() {
        String url = "" ; 
		client = new Client();
		serveur = Naming.lookup(url); 
        	sharedObjects = new HashMap<Integer,SharedObject>();
	}
	
	// lookup in the name server
	public static SharedObject lookup(String name) {
		int id = serveur.lookup(name);
        if (id != -1 ) {
            SharedObject s = sharedObjects.get(id) ;
            if (s!=null) {
                return s;
            }
            else {
                s = new SharedObject(id) ;
                sharedObjects.put(id,s) ;
                return s;
            }
	}
	}		
	
	// binding in the name server
	public static void register(String name, SharedObject_itf so) {
		SharedObject s = (SharedObject) so ;
		int id = s.getId() ;
		serveur.register(name,id) ;
	}

	// creation of a shared object
	public static SharedObject create(Object o) {
		int id = serveur.create(o) ;
		SharedObject so ; 
        if (id != -1) {
            so = new SharedObject(id);
            sharedObjects.put(id,so);
        }
		return so ;
		
	}
	
/////////////////////////////////////////////////////////////
//    Interface to be used by the consistency protocol
////////////////////////////////////////////////////////////

	// request a read lock from the server
	public static Object lock_read(int id) {
        //shared object doit etre en NL
		try {
			return serveur.lock_read(id,client);
		}
		catch (RemoteException e) {
		}
	}

	// request a write lock from the server
	public static Object lock_write (int id) {
        //shared object doit etre en NL ou RLC
		try {
			return serveur.lock_write(id,client);
		}
		catch (RemoteException e) {}
	}

	// receive a lock reduction request from the server
	public Object reduce_lock(int id) throws java.rmi.RemoteException {
		SharedObject s = sharedObjects.get(id);			 
        //redirection de l'appel vers le sharedobject	
		return s.reduce_lock(); 
	}


	// receive a reader invalidation request from the server
	public void invalidate_reader(int id) throws java.rmi.RemoteException {
		SharedObject s = sharedObjects.get(id);
        //redirection de l'appel vers le sharedobject	
		s.invalidate_reader();
	}


	// receive a writer invalidation request from the server
	public Object invalidate_writer(int id) throws java.rmi.RemoteException {
		SharedObject s = sharedObjects.get(id) ;
        //redirection de l'appel vers le sharedobject	
		return s.invalidate_writer();
	}
}
