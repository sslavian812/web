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


/**
 * Created by Sergey on 14.12.2014.
 */
@WebServlet(name = "servlet.SignUpServlet", urlPatterns = "/signup")
public class SignUpServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_PASSWORD = "password";

    public static final String COOKIE_AUTH = "auth";

    /**
     * Handles signup request, checks it for validity and if it's OK registers the new user.
     * Sends an auth cookie for to the user.
     * @param request should look like "/signup?username=...&password=..."
     * @param response contains an auth cookie if the signing up was successful
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter(PARAM_USERNAME);
        String password = request.getParameter(PARAM_PASSWORD);
        if (username == null || username.length() == 0
         || password == null || password.length() == 0) {
            writeResult(ERR_CODE_BAD_REQUEST, response);
            return;
        }
        long id;
        try {
            DBAdapter.connect();
            User existingUser = DBAdapter.getUserByLogin(username);
            id = DBAdapter.addUser(username, password);
            if (existingUser != null) {
                writeResult(ERR_CODE_USERNAME_NOT_UNIQUE, response);
                return;
            }
        } catch (SQLException | ClassNotFoundException e) {
            writeResult(ERR_INTERNAL, response);
            return;
        }
        if (id == -1) {
            writeResult(ERR_INTERNAL, response);
        } else {
            String cookie = CookieManager.makeCookie(id);
            if (cookie == null) {
                writeResult(ERR_INTERNAL, response);
                return;
            }
            response.addCookie(new Cookie(COOKIE_AUTH, cookie));
            writeResult(CODE_OK, response);
        }
    }

    public static final int CODE_OK = 100;
    public static final int ERR_CODE_BAD_REQUEST = 300;
    public static final int ERR_CODE_USERNAME_NOT_UNIQUE = 301;
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
