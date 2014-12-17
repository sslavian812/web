package servlet;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Sergey on 14.12.2014.
 */
@WebServlet(name = "servlet.TestServlet", urlPatterns = "/test")
public class TestServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletOutputStream out = response.getOutputStream();
        out.println("Path: " + request.getPathInfo());
        response.addHeader("Access-Control-Allow-Origin", "*");

        out.println(request.getCookies().length + " cookies");
        for (Cookie c : request.getCookies()) {
            out.println(c.getName() + " = " + c.getValue());
        }
        out.println("Query: " + request.getQueryString());
        response.addCookie(new Cookie("name", "value"));
    }
}
