import java.awt.*;
import java.awt.event.*;
import java.rmi.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.rmi.registry.*;


public class Irc extends Frame {
    public TextArea text;
    public TextField data;

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
        new Irc(name, true, sentence);
    }

    public Irc(String name, boolean useTransactions, SharedObject sentence) {
        this.name = name;
        this.useTransactions = useTransactions;
        this.sentence = sentence;

        // Create grapgical template
        setLayout(new FlowLayout());

        text = new TextArea(10, 60);
        text.setEditable(false);
        text.setForeground(Color.red);
        add(text);

        data=new TextField(60);
        add(data);

        Button write_button = new Button("write");
        write_button.addActionListener(new writeListener(this));
        add(write_button);
        Button read_button = new Button("read");
        read_button.addActionListener(new readListener(this));
        add(read_button);

        setSize(500, 300);
        text.setBackground(Color.black);
        show();
    }
}



class readListener implements ActionListener {
    Irc irc;

    public readListener(Irc irc) {

        this.irc = irc;
    }

    public void actionPerformed (ActionEvent e) {
        String s = new String();
        boolean ok = false;

        if(irc.useTransactions) {

            Transaction t = null;

            try {
                t = new Transaction();
                t.start();

                irc.sentence.lock_read();
                s = ((Sentence)irc.sentence.obj).read();

                ok = t.commit();
            }
            catch(Exception err) {

                t.abort();
                ok = false;
            }
        }

        if(!irc.useTransactions || !ok) {

            irc.sentence.lock_read();

            s = ((Sentence)(irc.sentence.obj)).read();

            irc.sentence.unlock();
        }

        irc.text.append(s + "\n");
    }
}

class writeListener implements ActionListener {
    Irc irc;
    public writeListener (Irc i) {
            irc = i;
    }
    public void actionPerformed (ActionEvent e) {
        String s = irc.data.getText();
        boolean ok = false;

        if(irc.useTransactions) {

            Transaction t = null;

            try {
                t = new Transaction();
                t.start();

                irc.sentence.lock_write();
                ((Sentence)irc.sentence.obj).write(irc.name + " wrote : " + s);

                ok = t.commit();
            }
            catch(Exception err) {

                t.abort();
                ok = false;
            }
        }

        if(!irc.useTransactions || !ok) {

            irc.sentence.lock_write();

            ((Sentence)irc.sentence.obj).write(irc.name + " wrote : " + s);

            irc.sentence.unlock();
        }

        irc.data.setText("");
    }
}



