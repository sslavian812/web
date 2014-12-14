package db;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");

        try {
            DBAdapter.connect();

            DBAdapter.drop();


            DBAdapter.addUser("u1", "p1");
            DBAdapter.addUser("u2", "p2");

            User u1 = DBAdapter.getUser("u1", "p1");
            System.out.println(u1.toString());

            User u2 = DBAdapter.getUser("u2", "p2");
            System.out.println(u2.toString());


            DBAdapter.addCookie(u1.id, "u1_cookie");

            System.out.println("id from cookie: " + DBAdapter.getUserIdFromCookie("u1_cookie"));
            System.out.println("cookie by user: " + DBAdapter.getCookieFromUser(u1.id));

            System.out.println();

            String mylist = "mylist";
            DBAdapter.addList(u2.id, mylist);
            WordsList wl = DBAdapter.getList(u2.id, mylist);
            System.out.println(wl.toString());
            DBAdapter.addWord(wl.id, "word1", "transl", "json ololo{}");
            DBAdapter.addWord(wl.id, "word2", "transl", "json ololo{}");

            List<Word> list = DBAdapter.getAllWordsFromList(wl.id);
            for(Word w : list)
                System.out.println(w.toString());

            System.out.println();



            DBAdapter.close();
        } catch (Exception exc) {
            exc.printStackTrace();
            System.exit(1);
        }

    }
}
