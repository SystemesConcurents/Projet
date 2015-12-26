public class SentenceAnnotated_stub extends SharedObject implements SentenceAnnotated_itf, java.io.Serializable
{
	public SentenceAnnotated_stub(int id)
	{
		super(id);
	}
	public void write(String a1)
	{
		lock_write();
		SentenceAnnotated o = (SentenceAnnotated)obj;
		o.write(a1);
		unlock();
	}
	public String read()
	{
		lock_read();
		SentenceAnnotated o = (SentenceAnnotated)obj;
		String result = o.read();
		unlock();
		return result;
	}
}
