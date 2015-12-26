public class ItfGenerator
{
    public static void main(String argv[])
    {
        if(argv.length != 1 || argv[0].equals("-h") || argv[0].equals("--help"))
        {
            System.out.println("java ItfGenerator <class>");
            System.out.println("");
            System.out.println("Generate the <class>_itf interface.");
            return;
        }

        try
        {
            String class_name = argv[0];
            String itf = Generator.generateItf(Class.forName(class_name));
            System.out.println(itf);
        }
        catch(ClassNotFoundException e) {
            System.out.println(e);
        }
    }
}
