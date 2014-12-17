package servlet;

import db.DBAdapter;
import db.User;
import db.Word;
import db.WordsList;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        String auth = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName() == COOKIE_AUTH)
                auth = cookie.getValue();
        }

        if (auth == null)
            return -1;

        try {
            DBAdapter.connect();
            User user = DBAdapter.getUserByAuthCookie(auth);
            if (user == null)
                return -1;
            else
                return user.id;

        } catch (SQLException | ClassNotFoundException e) {
            return -1;
        } finally {
            try {
                DBAdapter.close();
            } catch (SQLException | ClassNotFoundException e) {
            }
        }
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
        if (!"get".equals(action) && !"delete".equals(action)) {
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
                } else if (ACTION_DELETE.equals(action) && list_id != -1) {
                    DBAdapter.deleteList(list_id);
                } else {
                    writeResult(ERR_CODE_BAD_REQUEST, response);
                    return;
                }

            } catch (SQLException | ClassNotFoundException e) {
                writeResult(ERR_INTERNAL, response);
                return;
            } finally {
                try {
                    DBAdapter.close();
                } catch (SQLException | ClassNotFoundException e) {
                }
            }
            writeResult(CODE_OK, response);
        }else if("word".equals(obj))
        {
            //add or remove words
            if (word == null || word.length() == 0) {
                writeResult(ERR_CODE_BAD_REQUEST, response);
                return;
            }
            try {
                DBAdapter.connect();

                int list_id = DBAdapter.getListId(id, list_name);
                int word_id = DBAdapter.getWordID(word);
                if(list_id == -1 || word_id == -1)
                {
                    writeResult(ERR_CODE_BAD_REQUEST, response);
                    return;
                }

                if (ACTION_ADD.equals(action)) {
                    DBAdapter.addWord(list_id, word_id); // todo: add method to DB Interface
                } else if (ACTION_DELETE.equals(action)) {
                    DBAdapter.deleteWord(word_id);
                } else {
                    writeResult(ERR_CODE_BAD_REQUEST, response);
                    return;
                }

            } catch (SQLException | ClassNotFoundException e) {
                writeResult(ERR_INTERNAL, response);
                return;
            } finally {
                try {
                    DBAdapter.close();
                } catch (SQLException | ClassNotFoundException e) {
                }
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

        if (list_name == null || list_name.length() == 0) {
            // get all lists
            try {
                DBAdapter.connect();
                List<WordsList> lists = DBAdapter.getAllListsFromUser(id);

                List<String> content = new ArrayList<>();

                for (WordsList wl : lists) {
                    content.add(wl.name);
                }

                JSONObject errorObj = new JSONObject(content); // do I need to add OK-code here?
                try {
                    ServletOutputStream out = response.getOutputStream();
                    out.print(errorObj.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (SQLException | ClassNotFoundException e) {
                writeResult(ERR_INTERNAL, response);
                return;
            } finally {
                try {
                    DBAdapter.close();
                } catch (SQLException | ClassNotFoundException e) {
                }
            }
            return;
        }

        try {
            // get all lists in list list_name
            DBAdapter.connect();
            int list_id = DBAdapter.getListId(id, list_name);
            if (list_id == -1) {
                writeResult(ERR_INTERNAL, response);
                return;
            }

            List<Word> words = DBAdapter.getAllWordsFromList(list_id);
            Map<String, String> map = new HashMap<>();

            for (Word w : words) {
                map.put(w.word, w.translation);
            }

            JSONObject errorObj = new JSONObject(map);    // do I need to add OK-code here?
            try {
                ServletOutputStream out = response.getOutputStream();
                out.print(errorObj.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    DBAdapter.close();
                } catch (SQLException | ClassNotFoundException e) {
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            writeResult(ERR_INTERNAL, response);
            return;
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
            ServletOutputStream out = response.getOutputStream();
            out.print(errorObj.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
