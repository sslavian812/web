web
===

Сервер работает на java и servlet'ах

База данных - SQLite, расположение:

{путь, где лежит бинарник Tomcat}/db/ss.s3db

Для её работы нужен SQLite JDBC -- https://bitbucket.org/xerial/sqlite-jdbc,
см. последний абзац.

API:
===
    /signup?username=u&password=p
	code:
        100 -- OK, see token
        300 -- bad request
        301 -- username already exists
        302 -- internal error
    token: cookie value to save as "auth"
    username: the user's name

	/signin?username=u&password=password (or with no args to handshake)
	code:
	    100 -- OK, see token
		101 -- "handshake" -- cookie is valid, you are %username%
	    300 -- bad request
	    301 -- access denied
	    302 -- internal error
    token: cookie value to save as "auth"
    username: the user's name

	/do?object=list&action=get
	-- получить все списки слов
	Пример ответа: ["history", "favorites", "lyrics"]

	/do?object=list&action=add&list=name
	создать список с именем name

	/do?object=list&action=delete&list=name
	удалить список с именем name

	/do?object=word&action=get&list=name
	получить все слова из списка юзера

	/do?object=word&actionadd=&list=name&word=word
	добавить слово word в список (слово уже должно содержаться в базе, например в списке history у даного юзера)

	/do?object=word&action=delete&list=name&word=word
	удалить слово word из списка name у текущего юзера

	/do?object=word&action=delete&list=*&word=word
	удалить слово word из всех списков юзера


	/translate?word=word
	перевести слово(текст) word. если юзер залогинен - слово добавится в его список history.