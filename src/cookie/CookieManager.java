package cookie;

import db.DBAdapter;
import db.User;

import java.sql.SQLException;
import java.util.Random;

/**
 * Created by Sergey on 16.12.2014.
 */
public class CookieManager {
    /**
     * Makes a random auth cookie for the user.
     * @param userId id of the user to make a cookie for
     */
    static Random rng = new Random();

    public static final int AUTH_COOKIE_LENGTH = 32;

    public static String makeCookie(long userId) {
        if (!DBAdapter.connect())
            return null;
        StringBuilder token = new StringBuilder(AUTH_COOKIE_LENGTH);
        for (int i = 0; i < AUTH_COOKIE_LENGTH; ++i)
            token.append((char)('a' + rng.nextInt('z' - 'a' + 1)));
        String value = token.toString();
        try {
            DBAdapter.addCookie(userId, value);
        } catch (SQLException e) {
            return null;
        }
        finally {
            DBAdapter.close();
        }
        return value;
    }

    public static User validateCookie(String cookie) {
        try {
            DBAdapter.connect();
            return DBAdapter.getUserByAuthCookie(cookie);
        } catch (SQLException e) {
            return null;
        }
        finally {
            DBAdapter.close();
        }
    }
}
