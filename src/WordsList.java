/**
 * Created by viacheslav on 13.12.14.
 */
public class WordsList {
    public static int id;
    public static int user_id;
    public static String name;

    WordsList(int id, int user_id, String name) {
        this.id = id;
        this.user_id = user_id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "<" + id + ", " + user_id + ", " + name + ">";
    }
}
