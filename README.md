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