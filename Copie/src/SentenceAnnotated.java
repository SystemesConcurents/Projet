public class SentenceAnnotated implements java.io.Serializable
{
    String data;

    public SentenceAnnotated()
    {
        data = new String("");
    }

    public void write(String text)
    {
        data = text;
    }

    public String read()
    {
        return data;
    }
}
