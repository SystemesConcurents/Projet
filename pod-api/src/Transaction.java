import java.util.Collection;
import java.util.ArrayList;

public class Transaction {

    private static Transaction currentTransaction = null;
    private Collection<SharedObject> relatedObjects;

    public Transaction() {

        this.relatedObjects = new ArrayList<SharedObject>();
    }

    public static Transaction getCurrentTransaction() {

        return currentTransaction;
    }

    public void addRelatedObject(SharedObject o) {
        relatedObjects.add(o);
    }

    // Indique si l'appelant est en mode transactionnel
    public boolean isActive() {

        return currentTransaction != null;
    }

    // Demarre une transaction (passe en mode transactionnel)
    public void start() {
        //System.out.println("---- Transaction initialized ----");

        if(currentTransaction != null) {
            throw new RuntimeException("A transaction is already started.");
        }
        currentTransaction = this;
    }

    // Termine une transaction et passe en mode non transactionnel
    public boolean commit(){

        currentTransaction = null;
        relatedObjects.clear();

        //System.out.println("---- Transaction commited ----");
        return false;
    }

    // Abandonne et annule une transaction (et passe en mode non transactionnel)
    public void abort(){

        for(SharedObject o : relatedObjects) {
            try {
                o.unlock();
                // Recharger les objets + remplacer les anciens par des nouveaux
            }
            catch(RuntimeException e) {
                System.out.println("RuntimeException in Transaction.abort().");
            }
        }

        //System.out.println("---- Transaction aborted ----");
        currentTransaction = null;
    }
}
