web
===

Сервер работает на Java и servlet'ах

База данных - SQLite, расположение:
    {путь, где лежит бинарник Tomcat}/db/ss.s3db

Для работы БД требуется [SQLite JDBC](https://bitbucket.org/xerial/sqlite-jdbc,)
Для корректной установки см. последний абзац.

API:
===

Регистрация и вход:
---

    /signup?username=u&password=p
    Зарегистрировать нового пользователя
	code:
        100 -- OK, see token
        300 -- bad request
        301 -- username already exists
        302 -- internal error
    token: cookie value to save as "auth"
    Пример ответа:
    	{"code":100, "username":"name", "token":"jccjunskexoqipxfcmytvzbvkezfpoux"}


	/signin?username=u&password=password (or with no args to handshake)
	Авторизовать пользователя
	code:
	    100 -- OK, see token
		101 -- "handshake" -- cookie is valid, you are %username%
	    300 -- bad request
	    301 -- access denied
	    302 -- internal error
    token: cookie value to save as "auth"
    username: the user's name
    Пример ответа:
    	{"code":100,"username":"name","token":"yhnsxgigyvysmnpycpofkypmnvxiqehm"}

    /signin
    Попытаться авторизовать пользователя по имеющейся cookie
    Пример ответа при удачной авторизации:
        {"code":100,"username":"name","token":"yhnsxgigyvysmnpycpofkypmnvxiqehm"}
    Ответ при неудачной авторизации
    	{"code":300}

Перевод:
---

	/translate?word=word
	Перевести слово word. если юзер залогинен - слово добавится в его список history.
	Пример отвера:
		{
			"word": "recombination",
			"translation": "рекомбинация",
			"article_json": {
				"head": {},
				"def": [{
					"pos": "noun",
					"text": "recombination",
					"tr": [{
						"ex": [{
							"text": "homologous recombination",
							"tr": [{"text": "гомологичная рекомбинация"}]
						}],
						"pos": "существительное",
						"text": "рекомбинация"
					}],
					"ts": "rɪkɒmbɪˈneɪʃn"
				}]
			}
		}

Списки слов, GET-запросы:
---

	/do?object=list&action=get
	Получить все списки слов
	Пример ответа:
		["history", "favorites", "lyrics"]

	/do?object=list&action=get&have_word=word
	Получить списки слов, в которых есть слово word
	Пример ответа:
    	["history", "favorites"]

	/do?object=list&action=get&not_have_word=word
	Получить списки слов, в которых нет слова word
	Пример ответа:
		["lyrics", "food"]

	/do?object=list&action=add&list=name - POST-запрос
	Создать список с именем name
	Пример ответа:
		{"code":100}

	/do?object=word&action=get&list=name
	Получить все слова из списка юзера
	Пример ответа:
		[
			{"translation":"первый","word":"first"},
			{"translation":"второй","word":"second"},
			{"translation":"третий","word":"third"}
		]

Списки слов, POST-запросы:
---

	/do?object=list&action=delete&list=name - POST-запрос
	Удалить список с именем name
	Пример ответа:
    	{"code":100}

	/do?object=word&actionadd=&list=name&word=word
	Добавить слово word в список (слово уже должно содержаться в базе, например в списке history у даного юзера)
	Пример ответа:
		{"code":100}

	/do?object=word&action=delete&list=name&word=word
	Удалить слово word из списка name у текущего юзера
	Пример ответа:
		{"code":100}

	/do?object=word&action=delete&list=*&word=word
	удалить слово word из всех списков юзера
	Пример ответа:
        {"code":100}
