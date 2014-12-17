package servlet;

import cookie.CookieManager;
import db.DBAdapter;
import db.User;
import db.Word;
import org.json.JSONArray;
import org.json.JSONObject;
import translate.Translator;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Sergey on 14.12.2014.
 * Written by Viacheslav on 17.12.2014.
 *
 */
@WebServlet(name = "servlet.TranslateServlet", urlPatterns = "/translate")
public class TranslateServlet extends HttpServlet {

    public static final String PARAM_WORD = "word";

    /**
     *
     * @param request
     * @return
     */

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        onRequest(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        onRequest(request, response);
    }

    private void onRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String text = request.getParameter(PARAM_WORD);

        if (text == null || text.length() == 0) {
            writeError(response);
            return;
        }
        try {
            DBAdapter.connect();
            Word word = DBAdapter.getWord(text);
            if (word == null) {
                word = Translator.translate(text);
            }

            int id = (int) CookieManager.identifyRequest(request);
            if (id != -1) {
                DBAdapter.connect();
                int list_id = DBAdapter.getListId(id, "history");
                DBAdapter.addWord(list_id, word.word, word.translation, word.article_json);
            }

            Map<String, Object> map = new HashMap<>();
            map.put("word", word.word);
            map.put("translation", word.translation);
            map.put("article_json", new JSONObject(word.article_json));

            JSONObject json = new JSONObject(map);
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            String result = json.toString(4).replace("\\", "");
            out.print(result);
        } catch (SQLException e) {
            e.printStackTrace();
            writeError(response);
        } finally {
            DBAdapter.close();
        }
    }

    public static final int ERR_INTERNAL = 302;

    private void writeError(HttpServletResponse response) {
        Map<String, Object> content = new HashMap<>();
        content.put("code", ERR_INTERNAL);
        JSONObject errorObj = new JSONObject(content);
        try {
            ServletOutputStream out = response.getOutputStream();
            out.print(errorObj.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
