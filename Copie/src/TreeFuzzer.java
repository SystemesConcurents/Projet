import java.util.UUID;

public class TreeFuzzer
{
    public static void main(String argv[])
    {
        Client.init();

        Tree_itf root = (Tree_itf)Client.lookup("Tree");
        if (root == null)
        {
            root = (Tree_itf)Client.create(new Tree());
            Tree_itf leaf = (Tree_itf)Client.create(new Tree());

            root.lock_write();
            root.setLeft((SharedObject)leaf);
            root.unlock();

            Client.register("Tree", root);
        }

        new TreeFuzzer(root);
    }

    public TreeFuzzer(Tree_itf root)
    {
        root.lock_read();
        while(true)
        {
            root.getLeft().lock_read();
            String s = null;
            for(int i = 0; i < 100; i++)
            {
                String s2 = ((Tree_itf) root.getLeft()).getLabel();
                if(s2 == null || (s != null && !s.equals(s2)))
                {
                    throw new RuntimeException("Inconsistency detected.");
                }
                s = s2;
            }
            root.getLeft().unlock();

            root.getLeft().lock_write();
            ((Tree_itf) root.getLeft()).setLabel(UUID.randomUUID().toString());
            root.getLeft().unlock();

            Thread.yield();
        }
    }
}
