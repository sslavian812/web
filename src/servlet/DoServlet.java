package servlet;

import cookie.CookieManager;
import db.DBAdapter;
import db.User;
import db.Word;
import db.WordsList;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Sergey on 14.12.2014.
 * Written by Viacheslav on 17.12.2014.
 */
@WebServlet(name = "servlet.DoServlet", urlPatterns = "/do")
public class DoServlet extends HttpServlet {
    public static final String PARAM_OBJECT = "object";
    public static final String PARAM_ACTION = "action";
    public static final String PARAM_LIST_NAME = "list";
    public static final String PARAM_WORD_NAME = "word";

    public static final String COOKIE_AUTH = "auth";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_GET = "get";
    public static final String ACTION_DELETE = "delete";


    int authenticateUser(HttpServletRequest request) {
        User user = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (Objects.equals(cookie.getName(), COOKIE_AUTH))
                user = CookieManager.validateCookie(cookie.getValue());
        }

        if (user == null)
            return -1;
        else
            return user.id;
    }

    //    _list/add/list
    //    _list/delete/list
    //    _word/add/list/word
    //    _word/delete/list(*)/word
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int id = authenticateUser(request);
        if (id == -1) {
            writeResult(ERR_CODE_AUTH_FAILURE, response);
            return;
        }

        String obj = request.getParameter(PARAM_OBJECT);
        if (!"list".equals(obj) && !"word".equals(obj)) {
            writeResult(ERR_CODE_BAD_REQUEST, response);
            return;
        }

        String action = request.getParameter(PARAM_ACTION);
        if (!"add".equals(action) && !"delete".equals(action)) {
            writeResult(ERR_CODE_BAD_REQUEST, response);
            return;
        }

        String list_name = request.getParameter(PARAM_LIST_NAME);
        String word = request.getParameter(PARAM_WORD_NAME);
        if (list_name == null || list_name.length() == 0) {
            writeResult(ERR_CODE_BAD_REQUEST, response);
            return;
        }


        if ("list".equals(obj)) {
            // add or delete lists by name
            try {
                DBAdapter.connect();

                int list_id = DBAdapter.getListId(id, list_name);

                if (ACTION_ADD.equals(action) && list_id == -1) {
                    DBAdapter.addList(id, list_name);
                } else if (ACTION_DELETE.equals(action) && list_id != -1 && !"history".equals(list_name)) {
                    DBAdapter.deleteList(list_id);
                } else {
                    writeResult(ERR_CODE_BAD_REQUEST, response);
                    return;
                }

            } catch (SQLException e) {
                writeResult(ERR_INTERNAL, response);
                return;
            } finally {

                DBAdapter.close();

            }
            writeResult(CODE_OK, response);
        } else if ("word".equals(obj)) {
            //add or remove words
            if (word == null || word.length() == 0) {
                writeResult(ERR_CODE_BAD_REQUEST, response);
                return;
            }
            try {
                DBAdapter.connect();

                int list_id = DBAdapter.getListId(id, list_name);
                int word_id = DBAdapter.getWordID(word);
                if (!"*".equals(list_name) && (list_id == -1 || word_id == -1)) {
                    writeResult(ERR_CODE_BAD_REQUEST, response);
                    return;
                }

                if (ACTION_ADD.equals(action)) {
                    DBAdapter.addWordToList(word_id, list_id);
                } else if (ACTION_DELETE.equals(action)) {
                    if ("*".equals(list_name)) {
                        List<WordsList> lists = DBAdapter.getAllListsFromUser(id);
                        for (WordsList wl : lists) {
                            DBAdapter.deleteWordFromList(word_id, wl.id);
                        }
                    }
                    DBAdapter.deleteWordFromList(word_id, list_id);
                } else {
                    writeResult(ERR_CODE_BAD_REQUEST, response);
                    return;
                }

            } catch (SQLException e) {
                writeResult(ERR_INTERNAL, response);
                return;
            } finally {
                DBAdapter.close();
            }
            writeResult(CODE_OK, response);
        }
    }

    //    _list/get/
    //    _word/get/list
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int id = authenticateUser(request);
        if (id == -1) {
            writeResult(ERR_CODE_AUTH_FAILURE, response);
            return;
        }

        String obj = request.getParameter(PARAM_OBJECT);
        if (!"list".equals(obj) && !"word".equals(obj)) {
            writeResult(ERR_CODE_BAD_REQUEST, response);
            return;
        }

        String action = request.getParameter(PARAM_ACTION);
        if (!"get".equals(action)) {
            writeResult(ERR_CODE_BAD_REQUEST, response);
            return;
        }


        String list_name = request.getParameter(PARAM_LIST_NAME);

        if ("list".equals(obj) && (list_name == null || list_name.length() == 0)) {
            // get all lists
            try {
                DBAdapter.connect();
                List<WordsList> lists = DBAdapter.getAllListsFromUser(id);

                List<String> content = new ArrayList<>();

                for (WordsList wl : lists) {
                    content.add(wl.name);
                }

                JSONArray resultObj = new JSONArray(content.toArray()); // do I need to add OK-code here?
                try {
                    PrintWriter out = response.getWriter();
                    out.print(resultObj.toString(4));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                writeResult(ERR_INTERNAL, response);
                return;
            } finally {
                DBAdapter.close();
            }
            return;
        } else if ("word".equals(obj)) {
            try {
                // get all lists in list list_name
                DBAdapter.connect();
                int list_id = DBAdapter.getListId(id, list_name);
                if (list_id == -1) {
                    writeResult(ERR_INTERNAL, response);
                    return;
                }

                List<Word> words = DBAdapter.getAllWordsFromList(list_id);

                List<Object> answerList = new ArrayList<>();

                for (Word w : words) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("word", w.word);
                    map.put("translation", w.translation);
                    answerList.add(map);
                }

                JSONArray resultObj = new JSONArray(answerList.toArray());    // do I need to add OK-code here?
                try {
                    response.setCharacterEncoding("UTF-8");
                    PrintWriter out = response.getWriter();
                    out.print(resultObj.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    DBAdapter.close();
                }
            } catch (SQLException e) {
                writeResult(ERR_INTERNAL, response);
            }
        } else {
            writeResult(ERR_CODE_BAD_REQUEST, response);
        }
    }

    public static final int CODE_OK = 100;
    public static final int ERR_CODE_BAD_REQUEST = 300;
    public static final int ERR_CODE_AUTH_FAILURE = 301;
    public static final int ERR_INTERNAL = 302;

    private void writeResult(int errorCode, HttpServletResponse response) {
        Map<String, Object> content = new HashMap<>();
        content.put("code", errorCode);
        JSONObject errorObj = new JSONObject(content);
        try {
            PrintWriter out = response.getWriter();
            out.print(errorObj.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
