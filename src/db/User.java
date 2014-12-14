package db;

/**
 * Created by viacheslav on 13.12.14.
 */
public class User {
    public int id;
    public String login;
    public String password;

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
