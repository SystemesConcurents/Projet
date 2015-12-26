public class StubGenerator
{
    public static void main(String argv[])
    {
        if(argv.length != 1 || argv[0].equals("-h") || argv[0].equals("--help"))
        {
            System.out.println("java StubGenerator <class>");
            System.out.println("");
            System.out.println("Generate the <class>_stub class.");
            return;
        }

        try
        {
            String class_name = argv[0];
            if(!class_name.endsWith("_itf"))
                class_name += "_itf";
            String stub = Generator.generateStub(Class.forName(class_name));
            System.out.println(stub);
        }
        catch(ClassNotFoundException e) {
            System.out.println(e);
        }
    }
}
