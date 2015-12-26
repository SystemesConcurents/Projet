public class Sentence_stub extends SharedObject implements Sentence_itf, java.io.Serializable
{
	public Sentence_stub(int id)
	{
		super(id);
	}
	public void write(String a1)
	{
		Sentence o = (Sentence)obj;
		o.write(a1);
	}
	public String read()
	{
		Sentence o = (Sentence)obj;
		String result = o.read();
		return result;
	}
}
