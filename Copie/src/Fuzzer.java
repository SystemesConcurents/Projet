import java.util.UUID;


public class Fuzzer
{
    public static void main(String argv[])
    {
        Client.init();

        Sentence_itf s = (Sentence_itf)Client.lookup("IRC");
        if (s == null)
        {
            s = (Sentence_itf)Client.create(new Sentence());
            Client.register("IRC", s);
        }

        new Fuzzer(s);
    }

    public Fuzzer(Sentence_itf so)
    {
        while(true)
        {
            so.lock_read();
            String s = null;
            for(int i = 0; i < 100; i++)
            {
                String s2 = so.read();
                if(s2 == null || (s != null && !s.equals(s2)))
                {
                    throw new RuntimeException("Inconsistency detected.");
                }
                s = s2;
            }
            so.unlock();
            //Thread.yield();
            /*
            try
            {
                Thread.sleep(1);
            }
            catch(Exception ex)
            {
            }
            */
            /*
            for(int i = 0; i < 100000; i++)
            {
                s = "trololo";
            }
            */
            so.lock_write();
            so.write(UUID.randomUUID().toString());
            so.unlock();
        }
    }
}
