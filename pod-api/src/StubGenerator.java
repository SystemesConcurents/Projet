import java.lang.reflect.*;
import java.lang.annotation.*;
import java.io.*;

public class StubGenerator {

    public static void main(String argv[]) throws ClassNotFoundException, FileNotFoundException, UnsupportedEncodingException {

        if(argv.length != 1) {
            System.out.println("java StubGenerator <class>");
            return;
        }

        String className = argv[0];
        Class aClass = Class.forName(className);

        // Generate itf
        String itfSource = new String("\n\n");
        itfSource += "public interface " + className + "_itf extends SharedObject_itf {\n";
        itfSource += "\n";
        for(Method method : aClass.getMethods()) {
            if(isValidMethod(method)) {
                itfSource += "\t" + signature(method) + ";\n";
                itfSource += "\n";
            }
        }
        itfSource += "}";

        // Generate stub
        String stubSource = new String("\n\n");
        stubSource += "public class " + className + "_stub";
        stubSource += " extends SharedObject";
        stubSource += " implements " + className + "_itf, java.io.Serializable {\n";
        stubSource += "\n";
        int methodsCount = 0;
        for(Method method : aClass.getMethods()) {
            if(isValidMethod(method)) {
                if(methodsCount++ != 0) {
                    stubSource += "\n";
                }
                stubSource += "\t" + signature(method) + " {\n";
                for(Annotation a : method.getDeclaredAnnotations()) {
                    if(a.annotationType() == Read.class) {
                        stubSource += "\n";
                        stubSource += "\t\tlock_read();\n";
                        break;
                    }
                    if(a.annotationType() == Write.class) {
                        stubSource += "\n";
                        stubSource += "\t\tlock_write();\n";
                        break;
                    }
                }
                stubSource += "\n";
                stubSource += "\t\t" + className + " o = (" + className + ")obj;\n";
                stubSource += "\n";
                stubSource += "\t\t";
                if(!method.getReturnType().equals(Void.TYPE)) {
                    stubSource += cleanType(method.getReturnType().getName()) + " r = ";
                }
                stubSource += "o." + method.getName() + "(";
                int parametersCount = 0;
                for(Class parameter : method.getParameterTypes()) {
                    if(parametersCount++ != 0) {
                        stubSource += ", ";
                    }
                    stubSource += String.valueOf(Character.toChars(65+parametersCount));
                }
                stubSource += ");\n";
                for(Annotation a : method.getDeclaredAnnotations()) {
                    if(a.annotationType() == Read.class || a.annotationType() == Write.class) {
                        stubSource += "\n";
                        stubSource += "\t\tunlock();\n";
                        break;
                    }
                }
                if(!method.getReturnType().equals(Void.TYPE)) {
                    stubSource += "\n";
                    stubSource += "\t\treturn r;\n";
                }
                stubSource += "\t}\n";
            }
        }
        stubSource += "}";

        PrintWriter itf = new PrintWriter(className + "_itf.java", "UTF-8");
        itf.print(itfSource);
        itf.close();

        PrintWriter stub = new PrintWriter(className + "_stub.java", "UTF-8");
        stub.print(stubSource);
        stub.close();

        return;
    }

    private static String signature(Method method) {

        String s = new String();
        s += "public " + cleanType(method.getReturnType().getName()) + " " + method.getName() + "(";
        int parametersCount = 0;
        for(Class parameter : method.getParameterTypes()) {
            if(parametersCount++ != 0) {
                s += ", ";
            }
            s += cleanType(parameter.getName()) + " " + String.valueOf(Character.toChars(65 + parametersCount));
        }
        s += ")";
        if(method.getExceptionTypes().length != 0) {
            s += " throws ";
            int exceptionsCount = 0;
            for(Class exception : method.getExceptionTypes()) {
                if(exceptionsCount++ != 0) {
                    s += ", ";
                }
                s += cleanType(exception.getName());
            }
        }

        return s;
    }

    private static String cleanType(String fullType) {

        String[] typeParts = fullType.split("\\.");
        String type = typeParts[typeParts.length - 1];

        return type;
    }

    private static boolean isValidMethod(Method m) {
        if(!Modifier.isPublic(m.getModifiers())) {
            return false;
        }
        String name = m.getName();
        return name != "wait" && name != "equals" && name != "toString"
                && name != "hashCode" && name != "getClass" && name != "notify"
                && name != "notifyAll";
    }
}
