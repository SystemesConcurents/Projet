import java.io.*;
import java.util.* ;
import java.util.concurrent.locks.ReentrantLock;
public class SharedObject implements Serializable, SharedObject_itf {
	
	private int id ;
	private Object o ;
	private int lock ;
	//NL, no lock 0
	//RLC, read lock cached  1
	//WLC, write lock cached 2
	//RLT, read lock taken 3
	//WLT, write lock taken 4
	//RLT_WLC  read lock taken and write lock cached 5

	
	public SharedObject(int id) {
		this.id = id ;
		this.lock=0 ; 
		this.o = null ; 
	}
	
	public int getId() { 
		return this.id;
	}
	
	public Object getObject() { 
		return this.o; 
	}
		
	
	// invoked by the user program on the client node
	public synchronized void lock_read() {		
		try {
			this.wait();
		}
		catch (InterruptedException e) {
		}
		switch (lock) {
            case 0:  
					 this.o = Client.lock_read(this.id) ;
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
			this.o = obj ; 
		}		
		this.lock = 4  ; //write lock taken 		

	}

	// invoked by the user program on the client node
	public synchronized void unlock() {		
		if (this.lock == 5 || this.lock == 4) {
			this.lock = 2 ;
		}
		else if (this.lock == 3) {
			this.lock = 1 ;
		}
		//on notifie que le verrou a été libéré
		try {
			this.notify() ; 
		}
		catch (Exception e) {
		}
	}


	// callback invoked remotely by the server
	public synchronized Object reduce_lock() {
		while (this.lock!=5 && this.lock!=2 && this.lock!=4) {
			wait();
		}
		if (this.lock==4 || this.lock==2) {
			this.lock=1;
		}
		else if (this.lock==5) {
			this.lock=3;
		}
		return this.o ; 
	}

	// callback invoked remotely by the server
	public synchronized void invalidate_reader() {
		while (this.lock==3 || this.lock==1) {

			wait();
		}
		this.lock=0; 
	}

	public synchronized Object invalidate_writer() {
		while (this.lock==4 || this.lock==5 || this.lock==3) {
			wait();
		}
		
		this.lock=0;
		return this.o ; 
		
	
	}
}
