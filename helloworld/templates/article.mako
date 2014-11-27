<%page args="article"/>
<%
    from helloworld.lib.translator import Article
    from helloworld.lib.translator import DictionaryArticle
    from helloworld.lib.translator import Translation
    from helloworld.lib.translator import DictionaryEntry
    from functools import reduce
%>
%if isinstance(article, Article):
    ${article.original} - ${article.main_translation} <br>
    %if isinstance(article, DictionaryArticle):
        ${'['+article.transcription+']' if len(article.transcription) > 0 else ''} <br><br>
        %for e in article.entries:
            ${e.position} <br>
            <blockquote>
                %for t in e.translations:
                    <hr>
                    ${t.text + (',' if len(t.synonyms) > 0 else '')}
                    ${reduce(lambda acc, x: acc + (', ' if len(acc) > 0 else '') + x, t.synonyms, '')}
                    <br>
                    <%
                        a = reduce(lambda acc, x: acc + (', ' if len(acc) > 0 else '') + x, t.meanings, '')
                    %>
                    %if len(a) > 0:
                        (${a})<br>
                    %endif
                    %for e in t.examples:
                        <blockquote>${e [0]} - ${e[1]}</blockquote>
                    %endfor
                %endfor
            </blockquote>
        %endfor
    %endif
%endif