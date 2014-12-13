public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");

        DBAdapter adapter = new DBAdapter();

        try {
            adapter.connect();
            adapter.createTables();
            adapter.addUser("vas", "vas123");
            adapter.getUser("vas", "vas123");


            System.out.println();

            adapter.addUser("pet", "pet123");

            adapter.getUser("vas", "pet123");
            adapter.getUser("vas", "vas123");
            adapter.getUser("pet", "pet123");


            adapter.close();
        } catch (Exception exc) {
            exc.printStackTrace();
            System.exit(1);
        }

    }
}
