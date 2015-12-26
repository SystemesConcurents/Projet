public class Tree_stub extends SharedObject implements Tree_itf, java.io.Serializable
{
	public Tree_stub(int id)
	{
		super(id);
	}
	public void setLabel(String a1)
	{
		Tree o = (Tree)obj;
		o.setLabel(a1);
	}
	public void setLeft(SharedObject a1)
	{
		Tree o = (Tree)obj;
		o.setLeft(a1);
	}
	public void setRight(SharedObject a1)
	{
		Tree o = (Tree)obj;
		o.setRight(a1);
	}
	public SharedObject getLeft()
	{
		Tree o = (Tree)obj;
		SharedObject result = o.getLeft();
		return result;
	}
	public SharedObject getRight()
	{
		Tree o = (Tree)obj;
		SharedObject result = o.getRight();
		return result;
	}
	public String getLabel()
	{
		Tree o = (Tree)obj;
		String result = o.getLabel();
		return result;
	}
}
