import java.io.*;
import java.util.* ;
import java.util.concurrent.locks.ReentrantLock;
public class SharedObject implements Serializable, SharedObject_itf {
	
	private int id ;
	private Object obj ;
	private int lock ;
	//NL, no lock 0
	//RLC, read lock cached  1
	//WLC, write lock cached 2
	//RLT, read lock taken 3
	//WLT, write lock taken 4
	//RLT_WLC  read lock taken and write lock cached 5

	public SharedObject() {
		this.id=0;
		this.obj=null;
		this.lock=0; 
	}
	
	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}
	
	
	public void setId(int id) {
		this.id=id;
	}

	public SharedObject(int id) {
		this.id = id ;
		this.lock=0 ; 
		this.setObj(null) ; 
	}
	
	public int getId() { 
		return this.id;
	}
	
	public Object getObject() { 
		return this.getObj(); 
	}
		
	
	// invoked by the user program on the client node
	public synchronized void lock_read() {		
		assert(this.lock!=4) ; 
		switch (lock) {
            case 0:  
					 this.setObj(Client.lock_read(this.id)) ;
					 this.lock = 3 ;				
                     break;
            case 1:  this.lock = 3 ;
                     break;
            case 2:  this.lock = 5;
                     break;
                     
        }
     
	
	}

	// invoked by the user program on the client node
	public synchronized void lock_write() {
		Object obj ;
		try {
			this.wait();
		}
		catch (InterruptedException e) {
	    }
		if (this.lock ==0 || this.lock == 1 || this.lock == 3) {
			obj = Client.lock_write(this.id);
			this.setObj(obj) ; 
		}		
		this.lock = 4  ; //write lock taken 		

	}

	// invoked by the user program on the client node
	public synchronized void unlock() {		
		assert(this.lock!=2);
		switch(this.lock) {
			case 3 : this.lock=1;
					 break ;
			case 4 : this.lock=2;
					 break ; 
			default :  
		}
		//on notifie que le verrou a été libéré
		try {
			notify() ; 
		}
		catch (Exception e) {
		}
	}


	// callback invoked remotely by the server
	public synchronized Object reduce_lock() {
		while (this.lock!=5 && this.lock!=2 && this.lock!=4) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (this.lock==4 || this.lock==2) {
			this.lock=1;
		}
		else if (this.lock==5) {
			this.lock=3;
		}
		return this.getObj() ; 
	}

	// callback invoked remotely by the server
	public synchronized void invalidate_reader() {
		while (this.lock==3 || this.lock==1) {

			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.lock=0; 
	}

	public synchronized Object invalidate_writer() {
		assert(this.lock!=3) ; 
		switch(this.lock) {
			case 4 : try {
				wait() ;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			case 2 : this.lock=0 ;
					 break ;
		    default : 
		}
		return this.getObj() ; 
					 
		
	
	}
}
