package db; /**
 * Created by viacheslav on 13.12.14.
 */

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
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
 * db.DBAdapter.connect();
 * db.DBAdapter.add..
 * db.DBAdapter.get..
 * db.DBAdapter.close()
 */
public class DBAdapter {
    public static Connection connection;
    public static Statement statement;
    public static ResultSet resultSet;

    /**
     * connects to DB
     */
    public static boolean connect() {
        connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:db/ss.s3db");
            statement = connection.createStatement();
            createTables();
            System.out.println("data base connected successfully!");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * creates tables
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static void createTables() throws SQLException {
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
     * @throws SQLException
     */
    public static boolean close() {
        try {
            resultSet.close();
            statement.close();
            connection.close();
            System.out.println("Database closed");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * clears all data in tables
     *
     * @throws SQLException
     */
    public static void drop() throws SQLException {
        statement.execute("DROP TABLE if exists 'users';");
        statement.execute("DROP TABLE if exists 'words';");
        statement.execute("DROP TABLE if exists 'lists';");
        statement.execute("DROP TABLE if exists 'words_in_lists';");
        statement.execute("DROP TABLE if exists 'cookies';");
        createTables();
        System.out.println("Database cleared");
    }

    //------------------------------------------------------------------------------------------------------------------


    /**
     * add a user to database, creates default "history" list.
     *
     * @param login
     * @param password
     * @throws SQLException
     */
    public static long addUser(String login, String password) throws SQLException {
        statement.execute("INSERT INTO 'users' ('login', 'password') VALUES ( '" + login + "' , '" + password + "'); ");
        ResultSet results = statement.getGeneratedKeys();
        if (results.next()) {
            long id = results.getLong(1);
            addList(id, "history");
            return id;
        }
        return -1;
    }


    /**
     * Returns user given his login.
     *
     * @param login
     * @return
     * @throws SQLException
     */
    public static User getUserByLogin(String login) throws SQLException {
        return getUserWhere("login='"+login+"'");
    }

    /**
     * returns user from table iff login and password match
     *
     * @param login
     * @param password
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static User getUserByCredentials(String login, String password) throws SQLException {
        return getUserWhere("login='"+login +"' AND password='"+password+"'");
    }


    /**
     * Gets the first user which meets given SQL WHERE-statement
     *
     * @param where SQL WHERE statement
     * @return User object or null if there's no such user
     * @throws SQLException
     */
    private static User getUserWhere(String where) throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM users " +
                "WHERE " + where +";");
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String lg = resultSet.getString("login");
            String pw = resultSet.getString("password");

            return new User(id, lg, pw);
        }
        return null;
    }

    /**
     * Get a user with the corresponding auth cookie or null if there's no such user.
     * @param authCookie auth cookie to choose the user by
     * @return User object or null if no user is found
     * @throws SQLException
     */
    public static User getUserByAuthCookie(String authCookie) throws SQLException {
        resultSet = statement.executeQuery(
                "SELECT * FROM cookies WHERE value=" + authCookie + ";"
        );
        if (resultSet.first()) {
            long id = resultSet.getLong("user_id");
            User result = getUserWhere("id="+id);
            return result;
        }
        return null;
    }


    /**
     * adds list to usr by his id.
     *
     * @param user_id
     * @param name
     * @throws SQLException
     */
    public static void addList(long user_id, String name) throws SQLException {
        statement.execute("INSERT INTO 'lists' ('user_id', 'name') VALUES (" + user_id + ", \'" + name + "\'); ");
    }

    /**
     * returns id of list by it's name owned by the user or -1 if doesn't exist
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
        return -1;
    }

    /**
     * returns db.WordsList object or null if now exists
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

            return wordsList;
        }
        return null;
    }

    /**
     * deletes specified list and all the bindings between it and words.
     * words itself are leaving in the database even when no list contains it.
     * @param list_id
     * @throws SQLException
     */
    public static void deleteList(int list_id) throws SQLException{
        statement.execute("DELETE FROM 'lists' WHERE id = '" + list_id  + "'); ");
        statement.execute("DELETE FROM 'words_in_lists' WHERE list_id = '" + list_id  + "'); ");
    }

    /**
     * inserts a word to words and associated it with specified list
     *
     * @param list_id
     * @param word
     * @param translation
     * @param article_json
     * @throws SQLException
     */
    public static void addWord(int list_id, String word, String translation, String article_json) throws SQLException {

        statement.execute("INSERT INTO 'words' ('word', 'translation', 'article_json') " +
                "VALUES ('" + word + "' , '" + translation + "' , '" + article_json + "'); ");

        int word_id = getWordID(word);

        statement.execute("INSERT INTO 'words_in_lists' ('list_id', 'word_id') " +
                "VALUES ('" + list_id + "' , '" + word_id + "');");

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

            return w;
        }
        return null;
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
     * deletes the words from database and destroys all the binding it to every list
     * @param word_id
     * @throws SQLException
     */
    public static void deleteWord(int word_id) throws SQLException {
        statement.execute("DELETE FROM 'words' WHERE id = '" + word_id  + "'); ");
        statement.execute("DELETE FROM 'words_in_lists' WHERE word_id = '" + word_id  + "'); ");
    }

    /**
     * returns all the words in the specified list
     *
     * @param list_id
     * @return
     * @throws SQLException
     */
    public static List<Word> getAllWordsFromList(int list_id) throws SQLException {
        resultSet = statement.executeQuery("SELECT words.id, word, translation, article_json FROM words JOIN words_in_lists " +
                "ON words_in_lists.list_id='" + list_id + "' AND words.id=words_in_lists.word_id;");

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
     *
     * @param user_id
     * @param value
     * @throws SQLException
     */
    public static void addCookie(long user_id, String value) throws SQLException {
        statement.execute("INSERT INTO 'cookies' ('user_id', 'value') VALUES ( " + user_id + " , '" + value + "'); ");
    }

    /**
     * deletes a cookie associated with specifies user.
     * @param user_id
     * @throws SQLException
     */
    public static void deleteCookie(int user_id) throws SQLException {
        statement.execute("DELETE FROM 'cookies' WHERE user_id = '" + user_id  + "'); ");
    }

    /**
     * return user id by cookie or -1 if not exists
     *
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
     * return all valid cookies by user id in a list
     *
     * @param user_id
     * @return
     * @throws SQLException
     */
    public static List<String> getCookieFromUser(int user_id) throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM cookies " +
                "WHERE user_id='" + user_id + "';");

        ArrayList<String> results = new ArrayList<>();
        while (resultSet.next()) {
            String val = resultSet.getString("value");
            results.add(val);
        }
        return results;
    }
}
