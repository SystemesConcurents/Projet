public class Tree implements java.io.Serializable
{
    static final long serialVersionUID = 42133755648979L;

    SharedObject left;
    SharedObject right;
    String label;

    public Tree()
    {
        label = new String("Empty.");
        left = null;
        right = null;
    }

    public void setLabel(String text)
    {
        label = text;
    }

    public void setLeft(SharedObject tree)
    {
        left = tree;
    }

    public void setRight(SharedObject tree)
    {
        right = tree;
    }

    public SharedObject getLeft()
    {
        return left;
    }

    public SharedObject getRight()
    {
        return right;
    }

    public String getLabel()
    {
        return label;
    }
}
