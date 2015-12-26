import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.util.HashMap;
import java.net.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.lang.reflect.*;

public class Client extends UnicastRemoteObject implements Client_itf
{
    private static String RMI_PATH = "//localhost/SharedObjects";
    public static final Logger log = Logger.getLogger("client");

    public static Client client = null;
    private static Server_itf server;
    private static HashMap<Integer, SharedObject> objects;
    private static ReentrantLock mutex;

    public Client() throws RemoteException
    {
        mutex = new ReentrantLock();
        objects = new HashMap<Integer, SharedObject>();
    }

    /**
     * Interface to be used by applications
     */

    // initialization of the client layer
    public static void init()
    {
        try
        {
            client = new Client();
            server = (Server_itf)Naming.lookup(RMI_PATH);
        }
        catch(RemoteException e) {
            throw new RuntimeException(e);
        }
        catch(NotBoundException e) {
            throw new RuntimeException(e);
        }
        catch(MalformedURLException e) {
            throw new RuntimeException(e);
        }

    }

    // lookup in the name server
    public static SharedObject lookup(String name)
    {
        log.info(String.format("lookup \"%s\"", name));

        try
        {
            LookupResponse lookup_res = server.lookup(name);
            int so_id = lookup_res.getId();
            SharedObject so = null;

            if(so_id >= 0)
            {
                mutex.lock();

                if(objects.containsKey(so_id))
                    so = objects.get(so_id);
                else
                {
                    Class<?> obj_class = lookup_res.getObjectClass();
                    Class<?> so_class = Class.forName(String.format("%s_stub", obj_class.getName()));
                    so = (SharedObject) so_class.getConstructor(Integer.TYPE).newInstance(so_id);
                    objects.put(so.getId(), so);
                }

                mutex.unlock();
            }

            return so;
        }
        catch(ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        catch(NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        catch(InstantiationException e) {
            throw new RuntimeException(e);
        }
        catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        catch(InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        catch(RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    // binding in the name server
    public static void register(String name, SharedObject_itf so_itf)
    {
        SharedObject so = (SharedObject) so_itf;
        log.info(String.format("register object %d as \"%s\"", so.getId(), name));

        try
        {
            server.register(name, so.getId());
        }
        catch(RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    // creation of a shared object
    public static SharedObject create(Object o)
    {
        log.info("create object");

        try
        {
            int so_id = server.create(o);
            Class<?> so_class = Class.forName(String.format("%s_stub", o.getClass().getName()));
            SharedObject so = (SharedObject) so_class.getConstructor(Integer.TYPE).newInstance(so_id);

            mutex.lock();
            objects.put(so.getId(), so);
            mutex.unlock();

            return so;
        }
        catch(ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        catch(NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        catch(InstantiationException e) {
            throw new RuntimeException(e);
        }
        catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        catch(InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        catch(RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Interface to be used by the consistency protocol
     */

    // request a read lock from the server
    public static Object lock_read(int id)
    {
        assert(id >= 0);
        log.info(String.format("lock_read object %d from the server", id));

        try
        {
            return server.lock_read(id, client);
        }
        catch(RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    // request a write lock from the server
    public static Object lock_write(int id)
    {
        assert(id >= 0);
        log.info(String.format("lock_write object %d from the server", id));

        try
        {
            return server.lock_write(id, client);
        }
        catch(RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    // receive a lock reduction request from the server
    public Object reduce_lock(int id) throws RemoteException
    {
        assert(id >= 0);
        SharedObject so;

        mutex.lock();
        so = objects.get(id);
        mutex.unlock();

        return so.reduce_lock();
    }

    // receive a reader invalidation request from the server
    public void invalidate_reader(int id) throws RemoteException
    {
        assert(id >= 0);
        SharedObject so;

        mutex.lock();
        so = objects.get(id);
        mutex.unlock();

        so.invalidate_reader();
    }

    // receive a writer invalidation request from the server
    public Object invalidate_writer(int id) throws RemoteException
    {
        assert(id >= 0);
        SharedObject so;

        mutex.lock();
        so = objects.get(id);
        mutex.unlock();

        return so.invalidate_writer();
    }

    // method used when we unserialize a SharedObject
    public static void internal_register(SharedObject so)
    {
        mutex.lock();
        objects.put(so.getId(), so);
        mutex.unlock();
    }
}
