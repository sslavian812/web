package db; /**
 * Created by viacheslav on 13.12.14.
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


/**
 * helps to work with database.
 * usage:
 *     db.DBAdapter d = new...
 *     d.connect();
 *     d.add..
 *     d.get..
 *     d.close()
 */
public class DBAdapter {
    public static Connection connection;
    public static Statement statement;
    public static ResultSet resultSet;

    /**
     * connects to DB
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static void connect() throws ClassNotFoundException, SQLException {
        connection = null;
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:ss.s3db");

        System.out.println("data base connected successfully!");
    }

    /**
     * creates tables
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static void createTables() throws SQLException {
        statement = connection.createStatement();

        statement.execute("CREATE TABLE if not exists 'users' " +
                "('id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "'login' text unique, " +
                "'password' text);");

        statement.execute("CREATE TABLE if not exists 'cookies' " +
                "('id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "'user_id' INTEGER unique, " +
                "'value' text unique, " +
                "FOREIGN KEY(user_id) REFERENCES users(id));");

        statement.execute("CREATE TABLE if not exists 'lists' " +
                "('id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "'user_id' INTEGER, " +
                "'name' text, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id));");

        statement.execute("CREATE TABLE if not exists 'words' " +
                "('id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "'word' text unique, " +
                "'translation' text, " +
                "'article_json' text);");

        statement.execute("CREATE TABLE if not exists 'words_in_lists' " +
                "('id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "'list_id' INTEGER, " +
                "'word_id' INTEGER, " +
                "FOREIGN KEY (list_id) REFERENCES lists(list_id), " +
                "FOREIGN KEY (word_id) REFERENCES words(word_id));");

        System.out.println("tables already exist or were successfully created!");
    }

    /**
     * closes the database
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static void close() throws ClassNotFoundException, SQLException {
        resultSet.close();
        statement.close();
        connection.close();

        System.out.println("Database closed");
    }

    /**
     * clears all data in tables
     *
     * @throws SQLException
     */
    public static void drop() throws SQLException {
        statement.execute("DROP TABLE if not exists 'users';");
        statement.execute("DROP TABLE if not exists 'words';");
        statement.execute("DROP TABLE if not exists 'lists';");
        statement.execute("DROP TABLE if not exists 'words_in_lists';");
        statement.execute("DROP TABLE if not exists 'cookies';");
        createTables();
    }


    /**
     * add a user to database, creates default "history" list.
     *
     * @param login
     * @param password
     * @throws SQLException
     */
    public static void addUser(String login, String password) throws SQLException {
        statement.execute("INSERT INTO 'users' ('login', 'password') VALUES ( '" + login + "' , '" + password + "'); ");

        User u = getUser(login, password);

        int id = u.id;

        addList(id, "history");

        System.out.println("added user: " + u.toString());
    }

    /**
     * returns user from table iff login and password match, throws NullPointerException otherwise
     *
     * @param login
     * @param password
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static User getUser(String login, String password) throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM users " +
                "WHERE login='" + login + "' AND password='" + password + "';");

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String lg = resultSet.getString("login");
            String pw = resultSet.getString("password");
            User u = new User(id, lg, pw);

            System.out.println("got user: " + u.toString());

            return u;
        }
        throw new NullPointerException();
    }

    /**
     * adds list to usr by his id.
     *
     * @param user_id
     * @param name
     * @throws SQLException
     */
    public static void addList(int user_id, String name) throws SQLException {
        statement.execute("INSERT INTO 'lists' ('user_id', 'name') VALUES (" + user_id + ", \'" + name + "\'); ");
    }

    /**
     * returns id of list by it's name owned by the user or thrown NullPointer if it doed not exist.
     *
     * @param user_id
     * @param name
     * @return
     * @throws SQLException
     */
    public static int getListId(int user_id, String name) throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM lists " +
                "WHERE user_id='" + user_id + "' AND name='" + name + "';");

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            return id;
        }
        throw new NullPointerException();
    }

    /**
     * returns db.WordsList object ot throws nullPointerException
     *
     * @param user_id
     * @param name
     * @return
     * @throws SQLException
     */
    public static WordsList getList(int user_id, String name) throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM lists " +
                "WHERE user_id='" + user_id + "' AND name='" + name + "';");

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            int uid = resultSet.getInt("user_id");
            String nm = resultSet.getString("name");
            WordsList wordsList = new WordsList(id, uid, nm);

            System.out.println("got user: " + wordsList.toString());

            return wordsList;
        }
        throw new NullPointerException();
    }

    /**
     * inserts a word to words and associcates it with specified list
     *
     * @param list_id
     * @param word
     * @param translation
     * @param article_json
     * @throws SQLException
     */
    public static void addWord(int list_id, String word, String translation, String article_json) throws SQLException {

        statement.execute("INSERT INTO 'words' ('word', 'translation', 'article_json') " +
                "VALUES (" + word + " , " + translation + " , " + article_json + "); ");

        int word_id = getWordID(word);

        statement.execute("INSERT INTO 'words_in_lists' ('list_id', 'word_id') " +
                "VALUES (" + list_id + " , " + word_id + "); ");

    }

    /**
     * return db.Word object, or null if it not exists
     *
     * @param word
     * @return
     * @throws SQLException
     */
    public static Word getWord(String word) throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM words " +
                "WHERE word='" + word + "';");

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String wd = resultSet.getString("word");
            String tr = resultSet.getString("translation");
            String js = resultSet.getString("article_json");

            Word w = new Word(id, wd, tr, js);

            System.out.println("got user: " + w.toString());

            return w;
        }
        throw new NullPointerException();
    }

    /**
     * returns id of word if exists. otherwise throws NullPointerException
     *
     * @param word
     * @return
     * @throws SQLException
     */
    public static int getWordID(String word) throws SQLException {
        Word w = getWord(word);
        return w.id;
    }

    /**
     * returns all the words in the specified list
     *
     * @param list_id
     * @return
     * @throws SQLException
     */
    public static List<Word> getAllWordsFromList(int list_id) throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM words " +
                "WHERE words_in_lists.list_id='" + list_id + "' AND words.id=words_in_lists.words_id;");

        List<Word> list = new ArrayList<Word>();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String wd = resultSet.getString("word");
            String tr = resultSet.getString("translation");
            String js = resultSet.getString("article_json");

            Word w = new Word(id, wd, tr, js);
            list.add(w);
        }
        return list;
    }

    /**
     * return all the Lists owned by user
     *
     * @param user_id
     * @return
     * @throws SQLException
     */
    public static List<WordsList> getAllListsFromUser(int user_id) throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM lists " +
                "WHERE user_id='" + user_id + "';");

        List<WordsList> list = new ArrayList<WordsList>();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            int uid = resultSet.getInt("user_id");
            String nm = resultSet.getString("name");
            WordsList wordsList = new WordsList(id, uid, nm);
            list.add(wordsList);
        }
        return list;
    }

    /**
     * associates a cookie-value with specified user
     * @param user_id
     * @param value
     * @throws SQLException
     */
    public static void addCookie(int user_id, String value) throws SQLException {
        statement.execute("INSERT INTO 'cookies' ('user_id', 'value') VALUES ( " + user_id + " , '" + value + "'); ");
    }


    /**
     * return user id by cookie or -1 if not exists
     * @param value
     * @return
     * @throws SQLException
     */
    public static int getUserIdFromCookie(String value) throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM cookies " +
                "WHERE value='" + value + "';");

        while (resultSet.next()) {
            int id = resultSet.getInt("user_id");

            return id;
        }
        return -1;
    }

    /**
     * return cookie-value by user id or null otherwise
     * @param user_id
     * @return
     * @throws SQLException
     */
    public static String getCookieFromUser(int user_id) throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM cookies " +
                "WHERE user_id='" + user_id + "';");

        while (resultSet.next()) {
            String val = resultSet.getString("value");

            return val;
        }
        return null;
    }
}
