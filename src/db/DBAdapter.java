package db; /**
 * Created by viacheslav on 13.12.14.
 */

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
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
        if (connection == null) {
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
        return true;
    }

    /**
     * creates tables
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static void createTables() throws SQLException {
        statement.execute("PRAGMA foreign_keys = ON");

        statement.execute("CREATE TABLE if not exists 'users' " +
                "('id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "'login' text unique, " +
                "'password' text)");

        statement.execute("CREATE TABLE if not exists 'cookies' " +
                "('id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "'user_id' INTEGER, " +
                "'auth' text unique, " +
                "FOREIGN KEY(user_id) REFERENCES users(id))");

        statement.execute("CREATE TABLE if not exists 'lists' " +
                "('id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "'user_id' INTEGER, " +
                "'name' text, " +
                "FOREIGN KEY (user_id) REFERENCES users(id))");

        statement.execute("CREATE TABLE if not exists 'words' " +
                "('id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "'word' text unique on conflict ignore, " +
                "'translation' text, " +
                "'article_json' text)");

        statement.execute("CREATE TABLE if not exists 'words_in_lists' " +
                "('id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "'list_id' INTEGER, " +
                "'word_id' INTEGER, " +
                "FOREIGN KEY (list_id) REFERENCES lists(list_id), " +
                "FOREIGN KEY (word_id) REFERENCES words(word_id));");

        statement.execute("CREATE TABLE if not exists 'tokens' " +
                "('id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "'user_id' INTEGER, " +
                "'token' text unique, " +
                "'expires' INTEGER " +
                "FOREIGN KEY(user_id) REFERENCES users(id));");

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
            connection = null;
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
        statement.execute("DROP TABLE if exists 'tokens';");
        createTables();
        System.out.println("Database cleared");
    }

    //------------------------------------------------------------------------------------------------------------------

    public static void setToken(int user_id, String value, long unix_time) throws SQLException
    {
        String pst ="INSERT INTO 'tokens' ('user_id', 'token', 'expires') VALUES ( ?, ?, ?); ";
        PreparedStatement s = connection.prepareStatement(pst);
        s.setInt(1, user_id);
        s.setString(2, value);
        s.setLong(3, unix_time);

        s.execute();
    }

    public static void updateToken(int user_id, String value, long unix_time) throws SQLException
    {
        String pst ="UPDATE 'tokens' SET 'token'=?, 'expires'=? WHERE  'user_id'=?; ";
        PreparedStatement s = connection.prepareStatement(pst);
        s.setString(1, value);
        s.setLong(2, unix_time);
        s.setInt(3, user_id);

        s.execute();
    }

    public static String getToken(int user_id, long curTime) throws SQLException
    {
        String pst ="SELECT  * FROM 'tokens' ('user_id', 'token', 'expires') WHERE 'user_id'=?;";
        PreparedStatement s = connection.prepareStatement(pst);
        s.setInt(1, user_id);
        resultSet = s.getResultSet();

        while (resultSet.next()) {
            long time = resultSet.getLong("expires");
            String value = resultSet.getString("token");
            if(time + 60 < curTime)
                return null;
            else
                return value;
        }
        return null;
    }


    public static final String DEFAULT_LIST_NAME = "history";

    /**
     * add a user to database, creates default "history" list.
     *
     * @param login
     * @param password
     * @throws SQLException
     */
    public static long addUser(String login, String password) throws SQLException {
        String sql = "INSERT INTO 'users' ('login', 'password') VALUES (? , ?)";
        PreparedStatement s = connection.prepareStatement(sql);
        s.setString(1, login);
        s.setString(2, password);
        s.executeUpdate();
        ResultSet results = s.getGeneratedKeys();
        if (results.next()) {
            long id = results.getLong(1);
            addList(id, DEFAULT_LIST_NAME);
            return id;
        }
        return -1;
    }


    /**
     * Returns user given his login.
     * Unsafe method. be careful.
     *
     * @param login
     * @return
     * @throws SQLException
     */
    public static User getUserByLogin(String login) throws SQLException {
        String sql = "SELECT * FROM users WHERE login=?";
        PreparedStatement s = connection.prepareStatement(sql);
        s.setString(1, login);
        resultSet = s.executeQuery();
        if (resultSet.next()) {
            int id = resultSet.getInt("id");
            String lg = resultSet.getString("login");
            String pw = resultSet.getString("password");

            return new User(id, lg, pw);
        }
        return null;
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
        String sql = "SELECT * FROM users WHERE login=? AND password=?";
        PreparedStatement s = connection.prepareStatement(sql);
        s.setString(1, login);
        s.setString(2, password);
        resultSet = s.executeQuery();
        if (resultSet.next()) {
            int id = resultSet.getInt("id");
            String lg = resultSet.getString("login");
            String pw = resultSet.getString("password");

            return new User(id, lg, pw);
        }
        return null;
    }

    /**
     * Get a user with the corresponding auth cookie or null if there's no such user.
     *
     * @param authCookie auth cookie to choose the user by
     * @return User object or null if no user is found
     * @throws SQLException
     */
    public static User getUserByAuthCookie(String authCookie) throws SQLException {
        String sql = "SELECT * FROM cookies WHERE auth=?";
        PreparedStatement s = connection.prepareStatement(sql);
        s.setString(1, authCookie);
        resultSet = s.executeQuery();
        if (resultSet.next()) {
            long id = resultSet.getLong("user_id");
            sql = "SELECT * FROM users WHERE id=?";
            s = connection.prepareStatement(sql);
            s.setLong(1, id);
            resultSet = s.executeQuery();
            if (resultSet.next()) {
                String lg = resultSet.getString("login");
                String pw = resultSet.getString("password");
                return new User((int) id, lg, pw);
            }
        }
        return null;
    }

    /**
     * Gets the first user which meets given SQL WHERE-statement
     *
     * @param where SQL WHERE statement
     * @return User object or null if there's no such user
     * @throws SQLException
     */
    /*private static User getUserWhere(String where) throws SQLException {
        String sql = "SELECT * FROM users WHERE ?";
        PreparedStatement s = connection.prepareStatement(sql);
        s.setString(1, where);
        resultSet = s.executeQuery();
        if (resultSet.next()) {
            int id = resultSet.getInt("id");
            String lg = resultSet.getString("login");
            String pw = resultSet.getString("password");

            return new User(id, lg, pw);
        }
        return null;
    }*/


    /**
     * adds list to usr by his id.
     *
     * @param user_id
     * @param name
     * @throws SQLException
     */
    public static void addList(long user_id, String name) throws SQLException {
        PreparedStatement s = connection.prepareStatement("INSERT INTO 'lists' ('user_id', 'name') VALUES (?, ?)");
        s.setLong(1, user_id);
        s.setString(2, name);
        s.executeUpdate();
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
        PreparedStatement s = connection.prepareStatement("SELECT * FROM lists WHERE user_id=? AND name=?");
        s.setLong(1, user_id);
        s.setString(2, name);
        resultSet = s.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt("id");
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
        PreparedStatement s = connection.prepareStatement("SELECT * FROM lists WHERE user_id=? AND name=?");
        s.setLong(1, user_id);
        s.setString(2, name);
        resultSet = s.executeQuery();

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
     *
     * @param list_id
     * @throws SQLException
     */
    public static void deleteList(int list_id) throws SQLException {
        PreparedStatement s = connection.prepareStatement("DELETE FROM 'lists' WHERE id = ?");
        s.setLong(1, list_id);
        s.executeUpdate();
        s = connection.prepareStatement("DELETE FROM 'words_in_lists' WHERE list_id = ?");
        s.setLong(1, list_id);
        s.executeUpdate();
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
        PreparedStatement s = connection.prepareStatement(
                "INSERT INTO 'words' ('word', 'translation', 'article_json') VALUES (? , ?, ?)");
        s.setString(1, word);
        s.setString(2, translation);
        s.setString(3, article_json);
        s.executeUpdate();
        ResultSet generated = s.getGeneratedKeys();
        if (generated.next()) {
            long wordId = generated.getLong(1);
            s = connection.prepareStatement("INSERT INTO 'words_in_lists' ('list_id', 'word_id') VALUES (?, ?)");
            s.setLong(1, list_id);
            s.setLong(2, wordId);
            s.executeUpdate();
        }
    }

    /**
     * inserts a word to words and associated it with specified list
     *
     * @param list_id
     * @param word_id
     * @throws SQLException
     */
    public static void addWordToList(int list_id, int word_id) throws SQLException {
        PreparedStatement s = connection.prepareStatement(
                "INSERT INTO 'words_in_lists' ('list_id', 'word_id') VALUES (?, ?");
        s.setLong(1, list_id);
        s.setLong(2, word_id);
        s.executeUpdate();
    }

    /**
     * return db.Word object, or null if it not exists
     *
     * @param word
     * @return
     * @throws SQLException
     */
    public static Word getWord(String word) throws SQLException {
        PreparedStatement s = connection.prepareStatement("SELECT * FROM words WHERE word=?");
        s.setString(1, word);
        resultSet = s.executeQuery();

        if (resultSet.next()) {
            int id = resultSet.getInt("id");
            String wd = resultSet.getString("word");
            String tr = resultSet.getString("translation");
            String js = resultSet.getString("article_json");

            return new Word(id, wd, tr, js);
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
        return w != null ? w.id : -1;
    }

    /**
     * deletes the words from database and destroys all the binding it to every list.
     * Suppose, we do need this method at all.
     * @param word_id
     * @throws SQLException
     */
    public static void deleteWord(int word_id) throws SQLException {
        PreparedStatement s = connection.prepareStatement("DELETE FROM 'words' WHERE id = ?)");
        s.setLong(1, word_id);
        s.executeUpdate();
    }

    /**
     * deletes the words from database and destroys all the binding it to every list
     *
     * @param word_id
     * @param list_id
     * @throws SQLException
     */
    public static void deleteWordFromList(int word_id, int list_id) throws SQLException
    {
        PreparedStatement s = connection.prepareStatement(
                "DELETE FROM 'words_in_lists' WHERE list_id = ? AND word_id = ?)");
        s.setLong(1, list_id);
        s.setLong(2, word_id);
        s.executeUpdate();
    }

    /**
     * returns all the words in the specified list
     *
     * @param list_id
     * @return
     * @throws SQLException
     */
    public static List<Word> getAllWordsFromList(int list_id) throws SQLException {
        PreparedStatement s = connection.prepareStatement(
                "SELECT words.id, word, translation, article_json FROM words JOIN words_in_lists " +
                "ON (words.id=words_in_lists.word_id) WHERE words_in_lists.list_id=?");
        s.setLong(1, list_id);
        resultSet = s.executeQuery();

        List<Word> list = new ArrayList<>();

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
        PreparedStatement s = connection.prepareStatement("SELECT * FROM lists " +
                "WHERE user_id=?");
        s.setLong(1, user_id);
        resultSet = s.executeQuery();

        List<WordsList> list = new ArrayList<>();

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
        PreparedStatement s = connection.prepareStatement("INSERT INTO 'cookies' ('user_id', 'auth') VALUES (?, ?)");
        s.setLong(1, user_id);
        s.setString(2, value);
        s.executeUpdate();
    }

    /**
     * deletes a cookie associated with specifies user.
     *
     * @param user_id
     * @throws SQLException
     */
    public static void deleteCookie(int user_id) throws SQLException {
        PreparedStatement s = connection.prepareStatement("DELETE FROM 'cookies' WHERE user_id = ?)");
        s.setLong(1, user_id);
        s.executeUpdate();
    }

    /**
     * return all valid cookies by user id in a list
     *
     * @param user_id
     * @return
     * @throws SQLException
     */
    public static List<String> getCookieByUserID(int user_id) throws SQLException {
        PreparedStatement s = connection.prepareStatement("SELECT * FROM cookies WHERE user_id=?");
        s.setLong(1, user_id);
        resultSet = s.executeQuery();

        ArrayList<String> results = new ArrayList<>();
        while (resultSet.next()) {
            String val = resultSet.getString("auth");
            results.add(val);
        }
        return results;
    }
}
