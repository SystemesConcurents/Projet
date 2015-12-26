public interface Tree_itf extends SharedObject_itf
{
    public void setLabel(String text);
    public void setLeft(SharedObject tree);
    public void setRight(SharedObject tree);
    public SharedObject getLeft();
    public SharedObject getRight();
    public String getLabel();
}
