public class LookupResponse implements java.io.Serializable
{
    private int id;
    private Class obj_class;

    public LookupResponse()
    {
        this.id = -1;
        this.obj_class = null;
    }

    public LookupResponse(int id, Class obj_class)
    {
        this.id = id;
        this.obj_class = obj_class;
    }

    public int getId()
    {
        return this.id;
    }

    public Class getObjectClass()
    {
        return this.obj_class;
    }
}
