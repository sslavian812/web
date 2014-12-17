package db;

/**
 * Created by viacheslav on 13.12.14.
 */
public class Word {
    public int id;
    public String word;
    public String translation;
    public String article_json;

    public Word(int id, String word, String translation, String article_json) {
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