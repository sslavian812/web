<%inherit file="common_page.mako">
    <%
        if word is not None:
            from helloworld.lib.translator import Translator
            result = Translator.get_any_article(word)
    %>
${(result.original + " - " + result.main_translation) if word is not None else "translate something"}
</%inherit>