<html>
<head>
    <meta charset="UTF-8">
</head>
    <%block name="header">
        <%include file="menu_bar.mako"></%include>
    </%block>

    ${self.body()}

    <%block name="footer">
        <%include file="footer.mako"></%include>
    </%block>
</html>