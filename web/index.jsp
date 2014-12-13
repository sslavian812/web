<%--
  Created by IntelliJ IDEA.
  User: Sergey
  Date: 13.12.2014
  Time: 21:46
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%! int counter = 0; %>
<html>
  <head>
    <title></title>
  </head>
  <body>
    Hello world! This page has been loaded <%=++counter%> times.
    <%if (counter %10 == 0) out.print("Another ten refreshes!");%>
  </body>
</html>
