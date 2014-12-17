web
===

короче сюда добавляем полезные ссылки.

сервер работает на java и servlet'ах

база данных - SQLite, расположение
{путь, где лежит бинарник Tomcat}/db/ss.s3db

API:
===
/signup?username=u&password=p
	100 -- OK, see cookies
	300 -- bad request
	301 -- username already exists
	302 -- internal error

/signin?username=u&password=password
	100 -- OK, see cookies
	300 -- bad request
	301 -- access denied
	302 -- internal error

/do?object=list&action=get
	получить все списки слов

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
