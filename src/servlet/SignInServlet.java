package servlet;

import cookie.CookieManager;
import db.DBAdapter;
import db.User;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Sergey on 14.12.2014.
 */
@WebServlet(name = "servlet.SignInServlet", urlPatterns = "/signin")
public class SignInServlet extends HttpServlet {

    public static final String COOKIE_AUTH = "auth";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    /**
     * Performs a user authentication.
     *
     * @param request  should look like "/signin?username=...&password=..."
     * @param response will contain a cookie or an error information
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        response.addHeader("Access-Control-Allow-Origin", "*");

        if (username == null || username.length() == 0
                || password == null || password.length() == 0) {

            Cookie[] cookies = request.getCookies();
            if (cookies != null)
                for (Cookie c : cookies) {
                    if (CookieManager.COOKIE_AUTH.equals(c.getName())) {
                        User u = CookieManager.validateCookie(c.getValue());
                        if (u != null) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("code", CODE_HANDSHAKE);
                            data.put("username", u.login);
                            response.getWriter().print(new JSONObject(data));
                            return;
                        }
                        break;
                    }
                }

            writeResult(ERR_CODE_BAD_REQUEST, response);
            return;
        }
        if (!DBAdapter.connect()) {
            writeResult(ERR_INTERNAL, response);
            return;
        }
        User user = null;
        try {
            user = DBAdapter.getUserByCredentials(username, password);
            DBAdapter.close();
        } catch (SQLException e) {
            e.printStackTrace();
            writeResult(ERR_INTERNAL, response);
            return;
        } finally {
            DBAdapter.close();
        }
        if (user == null) {
            writeResult(ERR_CODE_DENIED, response);
            return;
        }
        String cookie = CookieManager.makeCookie(user.id);
        if (cookie == null) {
            writeResult(ERR_INTERNAL, response);
            return;
        }
        response.addCookie(new Cookie(COOKIE_AUTH, cookie));
        Map<String, Object> data = new HashMap<>();
        data.put("code", CODE_OK);
        data.put("username", username);
        data.put("token", cookie);
        response.getWriter().print(new JSONObject(data));
    }

    public static final int CODE_OK = 100;
    public static final int CODE_HANDSHAKE = 101;
    public static final int ERR_CODE_BAD_REQUEST = 300;
    public static final int ERR_CODE_DENIED = 301;
    public static final int ERR_INTERNAL = 302;

    private void writeResult(int errorCode, HttpServletResponse response) {
        Map<String, Object> content = new HashMap<>();
        content.put("code", errorCode);
        JSONObject errorObj = new JSONObject(content);
        try {
            ServletOutputStream out = response.getOutputStream();
            out.print(errorObj.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
