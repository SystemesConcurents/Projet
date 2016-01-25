

public class Sentence_stub extends SharedObject implements Sentence_itf, java.io.Serializable {

	public void write(String B) {

		Sentence o = (Sentence)obj;

		o.write(B);
	}

	public String read() {

		Sentence o = (Sentence)obj;

		String r = o.read();

		return r;
	}
}