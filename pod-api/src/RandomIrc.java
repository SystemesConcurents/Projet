import java.awt.*;
import java.awt.event.*;
import java.rmi.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.rmi.registry.*;


public class RandomIrc extends Frame {
    public TextArea text;
    public TextField data;
    public Random rand;
    private Reader reader;
    private Writer writer;

    public String name;
    public boolean useTransactions;
    public SharedObject sentence;

    public static void main(String argv[]) {

        // Ensure arguments are correctly given
        if (argv.length != 1) {
            System.out.println("java Irc <name>");
            return;
        }
        String name = argv[0];

        // Initialize connection with server
        Client.init();

        // Access IRC object from server or create it
        SharedObject sentence = Client.lookup("IRC");
        if(sentence == null) {
            sentence = Client.create(new Sentence());
            Client.register("IRC", sentence);
        }

        // Create GUI
        new RandomIrc(name, true, sentence);
    }

    public RandomIrc(String name, boolean useTransactions, SharedObject sentence) {
        this.name = name;
        this.useTransactions = useTransactions;
        this.sentence = sentence;
        this.reader = new Reader(this);
        this.writer = new Writer(this);
        this.rand = new Random();

        // Create grapgical template
        setLayout(new FlowLayout());

        text = new TextArea(10, 60);
        text.setEditable(false);
        text.setForeground(Color.red);
        add(text);

        setSize(500, 300);
        text.setBackground(Color.black);
        show();

        randomAction();
    }

    public void randomAction() {

        while(true) {
            try {
                Thread.sleep(rand.nextInt((20 - 10) + 1) + 10);
            }
            catch(InterruptedException e) {}

            int readOrWrite = rand.nextInt((1 - 0) + 1) + 0;

            if(readOrWrite == 0) {
                reader.read();
            }
            else {
                writer.write(name);
            }
        }
    }
}



class Reader {
    RandomIrc irc;

    public Reader(RandomIrc irc) {

        this.irc = irc;
    }

    public void read() {
        String s = new String();
        boolean ok = false;

        if(irc.useTransactions) {

            Transaction t = null;

            t = new Transaction();
            t.start();

            irc.sentence.lock_read();
            s = ((Sentence)irc.sentence.obj).read();
            irc.sentence.unlock();

            ok = t.commit();
        }

        if(!irc.useTransactions || !ok) {

            irc.sentence.lock_read();

            s = ((Sentence)(irc.sentence.obj)).read();

            irc.sentence.unlock();
        }

        irc.text.append(s + "\n");
    }
}

class Writer {
    RandomIrc irc;
    public Writer(RandomIrc i) {
            irc = i;
    }
    public void write(String s) {
        boolean ok = false;

        if(irc.useTransactions) {

            Transaction t = null;

            t = new Transaction();
            t.start();

            irc.sentence.lock_write();
            ((Sentence)irc.sentence.obj).write(irc.name + " wrote : " + s);

            Random rand = new Random();
            int commitOrAbort = rand.nextInt((4 - 0) + 1) + 0;
            if(commitOrAbort == 0) {
                t.abort();
                ok = false;
            }
            else {
                irc.sentence.unlock();
                ok = t.commit();
            }
        }

        if(!irc.useTransactions || !ok) {

            irc.sentence.lock_write();

            ((Sentence)irc.sentence.obj).write(irc.name + " wrote : " + s);

            irc.sentence.unlock();
        }
    }
}



