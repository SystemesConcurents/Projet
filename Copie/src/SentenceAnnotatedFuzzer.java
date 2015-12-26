import java.util.UUID;


public class SentenceAnnotatedFuzzer
{
    public static void main(String argv[])
    {
        Client.init();

        SentenceAnnotated_itf s = (SentenceAnnotated_itf)Client.lookup("IRC");
        if (s == null)
        {
            s = (SentenceAnnotated_itf)Client.create(new SentenceAnnotated());
            Client.register("IRC", s);
        }

        new SentenceAnnotatedFuzzer(s);
    }

    public SentenceAnnotatedFuzzer(SentenceAnnotated_itf so)
    {
        while(true)
        {
            String s = so.read();
            //Thread.yield();
            so.write(UUID.randomUUID().toString());
        }
    }
}
