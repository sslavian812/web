/**
 * Created by viacheslav on 13.12.14.
 */
public class Word {
    public static int id;
    public static String word;
    public static String translation;
    public static String article_json;

    Word(int id, String word, String translation, String article_json) {
        this.id = id;
        this.word = word;
        this.translation = translation;
        this.article_json = article_json;
    }

    @Override
    public String toString() {
        return "<" + id + ":  " + word + " -- " + translation + ">";
    }
}