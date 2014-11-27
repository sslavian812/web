<%inherit file="common_page.mako">
    <%
        if word is not None:
            from helloworld.lib.translator import Translator
            result = Translator.get_any_article(word)
            article = result
    %>
    %if word is not None:
        <%include file="article.mako" args="article=result"/>
    %else:
        Feel free to translate something
    %endif
    <form action="/translate">
        <input type="text" name="text" title="What to trabslate?">
        <input type="submit" value="Translate">
    </form>
</%inherit>