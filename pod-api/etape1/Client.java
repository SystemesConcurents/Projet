import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.net.*;
import java.util.* ;


public class Client extends UnicastRemoteObject implements Client_itf {

	public static Client_itf client ;
	private static Server_itf server ;
	private static HashMap<Integer,SharedObject> sharedObjects ;


	public Client() throws RemoteException {
		super();
	}


///////////////////////////////////////////////////
//         Interface to be used by applications
///////////////////////////////////////////////////

	// initialization of the client layer
	public static void init() {
	System.out.println("initialisation");
        String url = "" ;
		try {
			client = new Client();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		try {
			server = (Server_itf) Naming.lookup("//localhost:1337/Server");
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
       		sharedObjects = new HashMap<Integer,SharedObject>();
	}

	// lookup in the name server
	public static SharedObject lookup(String name) {
		int id=0;
		try {
			id = server.lookup(name);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		SharedObject s = null;
                if (id != -1 ) {
                      s = sharedObjects.get(id) ;
                      if (s!=null) {
                           return s;
                      }
                      else {
                           s = new SharedObject(id) ;
                           sharedObjects.put(id,s) ;
                           return s;
                      }

                }
                return s;
     	}

	// binding in the name server
	public static void register(String name, SharedObject_itf so) {
		SharedObject s = (SharedObject) so ;
		int id = s.getId() ;
		try {
			server.register(name,id) ;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
//sharedObjects.put(id,s) ;
	}

	// creation of a shared object
	public static SharedObject create(Object o) {
		int id=0;
		try {
			id = server.create(o);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		SharedObject so = null;
        if (id != -1) {
            so = new SharedObject(id);
           // sharedObjects.put(id,so);
        }System.out.println("objet crée");
		return so ;

	}

/////////////////////////////////////////////////////////////
//    Interface to be used by the consistency protocol
////////////////////////////////////////////////////////////

	// request a read lock from the server
	public static Object lock_read(int id) {
        //shared object doit etre en NL
		Object object = null;
		try {
			System.out.println("demande lock_read du client" +id) ;
			object= server.lock_read(id,client);
		}
		catch (RemoteException e) {

		}
		return object ;
	}

	// request a write lock from the server
	public static Object lock_write (int id) {
        //shared object doit etre en NL ou RLC
		Object object = null;
		try {
			System.out.println("demande lock_write du client" +id) ;

			object = server.lock_write(id,client);
		}
		catch (RemoteException e) {}
		return object;
	}

	// receive a lock reduction request from the server
	public Object reduce_lock(int id) throws java.rmi.RemoteException {
			System.out.println("demande reduce_lock du client" +id) ;

		SharedObject s = sharedObjects.get(id);
        //redirection de l'appel vers le sharedobject
		return s.reduce_lock();
	}


	// receive a reader invalidation request from the server
	public void invalidate_reader(int id) throws java.rmi.RemoteException {
			System.out.println("demande invalidate_reader du client" +id) ;

		SharedObject s = sharedObjects.get(id);
        //redirection de l'appel vers le sharedobject
		s.invalidate_reader();
	}


	// receive a writer invalidation request from the server
	public Object invalidate_writer(int id) throws java.rmi.RemoteException {
			System.out.println("demande invalidate_writer du client" +id) ;

		SharedObject s = sharedObjects.get(id) ;
        //redirection de l'appel vers le sharedobject
		return s.invalidate_writer();
	}
}

