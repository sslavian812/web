package db;

/**
 * Created by viacheslav on 13.12.14.
 */
public class User {
    public static int id;
    public static String login;
    public static String password;

    User(int id, String login, String password) {
        this.id = id;
        this.login = login;
        this.password = password;
    }

    @Override
    public String toString() {
        return "<" + id + ", " + login + ", " + password + ">";
    }
}
