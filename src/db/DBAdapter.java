package db; /**
 * Created by viacheslav on 13.12.14.
 */

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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
     * @throws SQLException
     */
    public static void createTables() throws SQLException {
        statement.execute("PRAGMA foreign_keys = ON");

        statement.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "login TEXT UNIQUE, " +
                "password TEXT, "+
                "time INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP)");

        statement.execute("CREATE TABLE IF NOT EXISTS cookies (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "auth TEXT UNIQUE, " +
                "time INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP, "+
                "FOREIGN KEY(user_id) REFERENCES users(id))");

        statement.execute("CREATE TABLE IF NOT EXISTS lists (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "name TEXT, " +
                "time INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP, "+
                "FOREIGN KEY (user_id) REFERENCES users(id))");

        statement.execute("CREATE TABLE IF NOT EXISTS words (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "word TEXT UNIQUE ON CONFLICT IGNORE, " +
                "translation text, " +
                "article_json text, "+
                "time INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP)");

        statement.execute("CREATE TABLE IF NOT EXISTS words_in_lists (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "list_id INTEGER, " +
                "word_id INTEGER, " +
                "time INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP, "+
                "FOREIGN KEY (list_id) REFERENCES lists(id)," +
                "FOREIGN KEY (word_id) REFERENCES words(id))");

        statement.execute("CREATE TABLE IF NOT EXISTS tokens " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "token TEXT UNIQUE, " +
                "expires INTEGER, " +
                "time INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP, "+
                "FOREIGN KEY(user_id) REFERENCES users(id))");

        System.out.println("tables already exist or were successfully created!");
    }

    /**
     * closes the database
     */
    public static boolean close() {
        try {
            if (resultSet != null)
                resultSet.close();
            if (statement != null)
                statement.close();
            if (connection != null)
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
        statement.execute("DROP TABLE if exists users;");
        statement.execute("DROP TABLE if exists words;");
        statement.execute("DROP TABLE if exists lists;");
        statement.execute("DROP TABLE if exists words_in_lists;");
        statement.execute("DROP TABLE if exists cookies;");
        statement.execute("DROP TABLE if exists tokens;");
        createTables();
        System.out.println("Database cleared");
    }

    //------------------------------------------------------------------------------------------------------------------

    public static void setToken(int user_id, String value, long unix_time) throws SQLException
    {
        String pst ="INSERT INTO tokens (user_id, token, expires) VALUES ( ?, ?, ?)";
        PreparedStatement s = connection.prepareStatement(pst);
        s.setInt(1, user_id);
        s.setString(2, value);
        s.setLong(3, unix_time);

        s.execute();
    }

    public static void updateToken(int user_id, String value, long unix_time) throws SQLException
    {
        String pst ="UPDATE tokens SET token=?, expires=? WHERE  user_id=?";
        PreparedStatement s = connection.prepareStatement(pst);
        s.setString(1, value);
        s.setLong(2, unix_time);
        s.setInt(3, user_id);

        s.execute();
    }

    public static String getToken(int user_id, long curTime) throws SQLException
    {
        String pst ="SELECT user_id, token, expires FROM tokens WHERE user_id=?";
        PreparedStatement s = connection.prepareStatement(pst);
        s.setInt(1, user_id);
        resultSet = s.getResultSet();

        if (resultSet.next()) {
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
     * @throws SQLException
     */
    public static long addUser(String login, String password) throws SQLException {
        String sql = "INSERT INTO users (login, password) VALUES (? , ?)";
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
     * adds list to usr by his id.
     *
     * @throws SQLException
     */
    public static void addList(long user_id, String name) throws SQLException {
        PreparedStatement s = connection.prepareStatement("INSERT INTO lists (user_id, name) VALUES (?, ?)");
        s.setLong(1, user_id);
        s.setString(2, name);
        s.executeUpdate();
    }

    /**
     * returns id of list by it's name owned by the user or -1 if doesn't exist
     *
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
     * @throws SQLException
     */
    public static WordsList getList(int user_id, String name) throws SQLException {
        PreparedStatement s = connection.prepareStatement("SELECT * FROM lists WHERE user_id=? AND name=?");
        s.setLong(1, user_id);
        s.setString(2, name);
        resultSet = s.executeQuery();

        if (resultSet.next()) {
            int id = resultSet.getInt("id");
            int uid = resultSet.getInt("user_id");
            String nm = resultSet.getString("name");

            return new WordsList(id, uid, nm);
        }
        return null;
    }

    /**
     * deletes specified list and all the bindings between it and words.
     * words itself are leaving in the database even when no list contains it.
     *
     * @throws SQLException
     */
    public static void deleteList(int list_id) throws SQLException {
        PreparedStatement s = connection.prepareStatement("DELETE FROM lists WHERE id=?");
        s.setLong(1, list_id);
        s.executeUpdate();
        s = connection.prepareStatement("DELETE FROM words_in_lists WHERE list_id=?");
        s.setLong(1, list_id);
        s.executeUpdate();
    }

    /**
     * inserts a word to words and associated it with specified list
     *
     * @throws SQLException
     */
    public static void addWord(int list_id, String word, String translation, String article_json) throws SQLException {
        PreparedStatement s = connection.prepareStatement(
                "INSERT INTO words (word, translation, article_json) VALUES (? , ?, ?)");
        s.setString(1, word);
        s.setString(2, translation);
        s.setString(3, article_json);
        s.executeUpdate();
        ResultSet generated = s.getGeneratedKeys();
        if (generated.next()) {
            long wordId = generated.getLong(1);
            s = connection.prepareStatement("INSERT INTO words_in_lists (list_id, word_id) VALUES (?, ?)");
            s.setLong(1, list_id);
            s.setLong(2, wordId);
            s.executeUpdate();
        }
    }

    /**
     * inserts a word to words and associated it with specified list
     *
     * @throws SQLException
     */
    public static void addWordToList(int list_id, int word_id) throws SQLException {
        PreparedStatement s = connection.prepareStatement(
                "INSERT INTO words_in_lists (list_id, word_id) VALUES (?, ?)");
        s.setLong(1, list_id);
        s.setLong(2, word_id);
        s.executeUpdate();
    }

    /**
     * return db.Word object, or null if it not exists
     *
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
     * @throws SQLException
     */
    public static int getWordID(String word) throws SQLException {
        Word w = getWord(word);
        return w != null ? w.id : -1;
    }

    /**
     * deletes the words from database and destroys all the binding it to every list.
     * Suppose, we do need this method at all.
     *
     * @throws SQLException
     */
    public static void deleteWord(int word_id) throws SQLException {
        PreparedStatement s = connection.prepareStatement("DELETE FROM words WHERE id = ?");
        s.setLong(1, word_id);
        s.executeUpdate();
    }

    /**
     * deletes the words from database and destroys all the binding it to every list
     *
     * @throws SQLException
     */
    public static void deleteWordFromList(int word_id, int list_id) throws SQLException
    {
        PreparedStatement s = connection.prepareStatement(
                "DELETE FROM words_in_lists WHERE list_id = ? AND word_id = ?");
        s.setLong(1, list_id);
        s.setLong(2, word_id);
        s.executeUpdate();
    }

    /**
     * returns all the words in the specified list
     *
     * @throws SQLException
     */
    public static List<Word> getWordsFromList(int listId) throws SQLException {
        PreparedStatement s = connection.prepareStatement(
                "SELECT words.id, word, translation, article_json FROM words JOIN words_in_lists " +
                "ON (words.id=words_in_lists.word_id) WHERE words_in_lists.list_id=? ORDER BY words_in_lists.time DESC");
        s.setLong(1, listId);
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
     * @throws SQLException
     */
    public static List<WordsList> getListsByUserId(int userId) throws SQLException {
        PreparedStatement s = connection.prepareStatement(
                "SELECT * FROM lists WHERE user_id=? ORDER BY time");
        s.setLong(1, userId);
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

    public static List<WordsList> getListsForWord(long userId, long wordId, boolean listsIncludeWord) throws SQLException {
        PreparedStatement s = connection.prepareStatement(
                "SELECT list_id FROM words_in_lists WHERE word_id=?");
        s.setLong(1, wordId);
        resultSet = s.executeQuery();
        Set<Long> listsWordIsIn  = new HashSet<>();
        while (resultSet.next()) {
            long id = resultSet.getLong("list_id");
            if (!listsWordIsIn.contains(id))
                listsWordIsIn.add(id);
        }
        s = connection.prepareStatement("SELECT * FROM lists WHERE user_id=? ORDER BY time");
        s.setLong(1, userId);
        resultSet = s.executeQuery();
        List<WordsList> answer = new ArrayList<>();
        while (resultSet.next()) {
            long id = resultSet.getLong("id");
            if (!(listsWordIsIn.contains(id) ^ listsIncludeWord))
                answer.add(new WordsList((int)id, (int)resultSet.getLong("user_id"), resultSet.getString("name")));
        }
        return answer;
    }

    /**
     * associates a cookie-value with specified user
     *
     * @throws SQLException
     */
    public static void addCookie(long user_id, String value) throws SQLException {
        PreparedStatement s = connection.prepareStatement(
                "INSERT INTO cookies (user_id, auth) VALUES (?, ?)");
        s.setLong(1, user_id);
        s.setString(2, value);
        s.executeUpdate();
    }

    /**
     * deletes a cookie associated with specifies user.
     *
     * @throws SQLException
     */
    public static void deleteCookie(int user_id) throws SQLException {
        PreparedStatement s = connection.prepareStatement("DELETE FROM cookies WHERE user_id = ?");
        s.setLong(1, user_id);
        s.executeUpdate();
    }

    /**
     * return all valid cookies by user id in a list
     *
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
