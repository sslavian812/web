import db.DBAdapter;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");

        DBAdapter adapter = new DBAdapter();

        try {
            adapter.connect();
            adapter.createTables();
            adapter.addUser("vas", "vas123");
            adapter.getUserByCredentials("vas", "vas123");


            System.out.println();

            adapter.addUser("pet", "pet123");

            adapter.getUserByCredentials("vas", "pet123");
            adapter.getUserByCredentials("vas", "vas123");
            adapter.getUserByCredentials("pet", "pet123");


            adapter.close();
        } catch (Exception exc) {
            exc.printStackTrace();
            System.exit(1);
        }

    }
}
