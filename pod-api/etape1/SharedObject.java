import java.io.*;
import java.util.* ;
import java.util.concurrent.locks.ReentrantLock;
public class SharedObject implements Serializable, SharedObject_itf {

	private int id ;
	Object obj ;
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
			this.obj=Client.lock_read(this.id);
			this.lock=3;  			
			break;
		case 1:       this.lock=3;
		break;
		case 2:       this.lock = 5;
		return;
		case 3 :
		case 4 :      break ; 


		}
		this.lock=3; 
		System.out.println(lock) ;


	}

	// invoked by the user program on the client node
	public synchronized void lock_write() {
		switch(lock) {
		case 0 : this.obj = Client.lock_write(this.id);
		this.lock=4;
		break;
		case 1 : this.lock=4;
		break;
		case 2 : this.lock=4;
		case 3 :
		case 4 :
		case 5 : this.lock=4; 
		break; 
		}
		System.out.println(lock) ;


	}

	// invoked by the user program on the client node
	public synchronized void unlock() {		
		switch(this.lock) {
		case 3 : this.lock=1;
		notify(); 
		break ;
		case 5 : this.lock=2;
		notify();
		break;  

		case 4 : this.lock=2;
		notify();
		break ; 
		case 0 :
		case 1 : 
		case 2 :  

		}
		System.out.println("j'ai unlocké oklm"); 
		System.out.println(lock) ;

	}


	// callback invoked remotely by the server
	public synchronized Object reduce_lock() {
		switch(lock) {
		case 2 : this.lock=1;
		break ;
		case 3 : break; 
		case 5 : this.lock=3;
		break;
		case 4 : //on attend la fin de l'écriture
			try {
				wait();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (lock==2) this.lock=1;
			break;
		case 1 :
		case 0 : 
		}
		return this.obj; 	

	}

	// callback invoked remotely by the server
	public synchronized void invalidate_reader() {
		assert(this.lock!=3);
		switch(lock) {
		case 1 : this.lock=0;
		break; 
		case 2 : 
		case 3 : try {
			wait();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.lock=0; 
		case 4 : break; 
		case 5 : try {
			wait();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.lock=0;
		break;
		case 0 : 
		}
		this.obj=null; 	
		System.out.println(lock) ;

	}

	public synchronized Object invalidate_writer() {
		assert(this.lock!=3) ; 
		switch(this.lock) {
		case 5 : 
		case 4 : try {
			wait() ;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.lock=0; 
		case 2 : this.lock=0 ;
		break ;
		case 0 :
		case 1 : 
		case 3 : break;  
		}	System.out.println(lock) ;

		return this.getObj() ; 	


	}
}
