import java.lang.reflect.*;
import java.util.Arrays;
import java.lang.annotation.Annotation;

public class Generator
{
    private final static String[] ignored_methods = new String[] {"wait", "equals", "toString", "hashCode", "getClass", "notify", "notifyAll"};
    private final static String[] so_methods = new String[] {"lock_read", "lock_write", "unlock"};

    public static String cleanAutoImport(String className)
    {
        if(className.startsWith("java.lang."))
            return className.substring(10);

        return className;
    }

    public static String signature(Method met)
    {
        assert(Modifier.isPublic(met.getModifiers()));
        String src = String.format("public %s %s(", cleanAutoImport(met.getReturnType().getName()), met.getName());
        int nb_params = 0;

        for(Class param : met.getParameterTypes())
        {
            if(nb_params++ > 0) src += ", ";
            src += String.format("%s a%d", cleanAutoImport(param.getName()), nb_params);
        }

        src += ")";

        if(met.getExceptionTypes().length > 0)
        {
            src += " throws ";
            int nb_exceptions = 0;
            for(Class exc : met.getExceptionTypes())
            {
                if(nb_exceptions++ > 0) src += ", ";
                src += cleanAutoImport(exc.getName());
            }
        }

        return src;
    }

    public static String generateItf(Class c)
    {
        String src = new String();

        src += String.format("public interface %s_itf extends SharedObject_itf\n", c.getName());
        src += "{\n";

        for(Method met : c.getMethods())
        {
            if(Modifier.isPublic(met.getModifiers()) && !Arrays.asList(ignored_methods).contains(met.getName()))
            {
                int nb_params = 0;
                src += String.format("\t%s;\n", signature(met));
            }
        }

        src += "}";

        return src;
    }

    public static String generateStub(Class c)
    {
        assert(c.getName().endsWith("_itf"));
        String origin = c.getName().substring(0, c.getName().length()-4);
        String src = new String();

        src += String.format("public class %s_stub extends SharedObject", origin);
        src += String.format(" implements %s, java.io.Serializable\n", c.getName());
        src += "{\n";

        // constructor
        src += String.format("\tpublic %s_stub(int id)\n", origin);
        src += "\t{\n";
        src += "\t\tsuper(id);\n";
        src += "\t}\n";

        for(Method met : c.getMethods())
        {
            if(Modifier.isPublic(met.getModifiers()) && !Arrays.asList(ignored_methods).contains(met.getName()) && !Arrays.asList(so_methods).contains(met.getName()))
            {
                int nb_params = 0;
                src += String.format("\t%s\n", signature(met));
                src += "\t{\n";

                // annotation
                for(Annotation annot : met.getDeclaredAnnotations())
                {
                    if(annot.annotationType() == Read.class)
                        src += "\t\tlock_read();\n";
                    if(annot.annotationType() == Write.class)
                        src += "\t\tlock_write();\n";
                }

                src += String.format("\t\t%s o = (%s)obj;\n", origin, origin);

                // appel
                src += "\t\t";

                if(!met.getReturnType().equals(Void.TYPE))
                    src += String.format("%s result = ", cleanAutoImport(met.getReturnType().getName()));

                src += String.format("o.%s(", met.getName());

                nb_params = 0;
                for(Class param : met.getParameterTypes())
                {
                    if(nb_params++ > 0) src += ", ";
                    src += String.format("a%d", nb_params);
                }

                src += ");\n";

                // annotation
                for(Annotation annot : met.getDeclaredAnnotations())
                {
                    if(annot.annotationType() == Read.class || annot.annotationType() == Write.class)
                        src += "\t\tunlock();\n";
                }

                // return
                if(!met.getReturnType().equals(Void.TYPE))
                    src += "\t\treturn result;\n";

                src += "\t}\n";
            }
        }

        src += "}";

        return src;
    }
}
