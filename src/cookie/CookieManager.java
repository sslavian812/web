package cookie;

import db.DBAdapter;
import db.User;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Objects;
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

    public static final String COOKIE_AUTH = "auth";

    public static long identifyRequest(HttpServletRequest request) {
        User user = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies) {
                if (Objects.equals(cookie.getName(), COOKIE_AUTH))
                    user = CookieManager.validateCookie(cookie.getValue());
        }
        if (user == null)
            return -1;
        else
            return user.id;
    }
}
