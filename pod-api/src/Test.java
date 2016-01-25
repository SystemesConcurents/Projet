



class Test {

    public static void main(String[] argv) {
        int t = 0;;

        try {
            assert(t != 0);
            System.out.println(t);
        }
        catch(RuntimeException e) {
            System.out.println("Goodbye");
        }
    }
}
