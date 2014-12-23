Slate
===

Сервер работает на Java и servlet'ах

База данных - SQLite, расположение:
    {путь, где лежит бинарник Tomcat}/db/ss.s3db

Для работы БД требуется [SQLite JDBC](https://bitbucket.org/xerial/sqlite-jdbc,)
Для корректной установки см. последний абзац.

Инструкция
===
1. Установить [Tomcat 8](http://tomcat.apache.org/):
Windows: на официальном сайте есть инсталлер;
Ubuntu / CentOS: [инструкция](http://tecadmin.net/install-tomcat-8-on-centos-rhel-and-ubuntu/).
__ВАЖНО__ Удостовериться, что Tomcat имеет доступ на запись к папке, куда он сам установлен, и всему её содержимому.

2. Загрузить [SQLite JDBC Driver](https://bitbucket.org/xerial/sqlite-jdbc/downloads), поместить .jar в
_{путь к Tomcat}/lib_.
Туда же распаковать из загруженного .jar: _org/sqlite/native/{Your platform}/{Your architecture}/sqlitejdbc..._

3. Создать _{путь к Tomcat}/bin/db/ss.s3db_ -- пустой файл.

4. Открыть проект в IntelliJ IDEA Ultimate.
5. Создать конфигурацию запуска Tomcat Server, указать ей Application Server и 
на вкладке Deployment добавить Artifact.

API
===

Регистрация и вход
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


    /signin?username=u&password=password (или без аргументов, чтобы по cookie получить имя пользователя)
    Авторизовать пользователя
	code:
	    100 -- OK, токен выдан
	    101 -- "рукопожитие" -- cookie валидна, выдано имя пользователя (без токена)
	    300 -- некорректный запрос
	    301 -- отказано в доступе
	    302 -- внутренняя ошибка
    token: Этот токен должен быть сохранен в cookies как "auth" и должен прикладываться параметром во все запросы к API.    	    username: Имя пользователя, совершившего вход.
    Пример ответа:
    	{"code":100,"username":"name","token":"yhnsxgigyvysmnpycpofkypmnvxiqehm"}

    /signin
    Попытаться авторизовать пользователя по имеющейся cookie
    Пример ответа при удачной авторизации:
        {"code":100,"username":"name","token":"yhnsxgigyvysmnpycpofkypmnvxiqehm"}
    Ответ при неудачной авторизации
    	{"code":300}

Перевод
---

	/translate?word=word&token=hereYourTokenPlease
	Перевести слово word. Если юзер залогинен, слово добавится в его список history.
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
		
	Коды ошибок:
	300 - некорректный запрос
	302 - внутренняя ошибка

Списки слов, GET-запросы
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

	/do?object=word&action=get&list=name
	Получить все слова из списка юзера
	Пример ответа:
		[
			{"translation":"первый","word":"first"},
			{"translation":"второй","word":"second"},
			{"translation":"третий","word":"third"}
		]
		
    Коды ошибок:
	300 - некорректный запрос
	301 - отказано в доступе
	302 - внутренняя ошибка

Списки слов, POST-запросы
---

    /do?object=list&action=add&list=name
	Создать список с именем name
	Пример ответа:
		{"code":100}

	/do?object=list&action=delete&list=name
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
        
    Коды ошибок:
	300 - некорректный запрос
	301 - отказано в доступе
	302 - внутренняя ошибка
