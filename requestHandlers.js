var querystring = require("querystring");



// main page
function hello(response, PostData, db) {
  console.log("Request handler 'hello' was called.");

  var body = '<html>'+
    '<head>'+
    '<meta http-equiv="Content-Type" content="text/html; '+
    'charset=UTF-8" />'+
    '</head>'+
    '<body>'+
    '<h1>hello!</h1>' +
    'use /register to reginster a new user or /send to send messages '
    '</body>'+
    '</html>';
    response.writeHead(200, {"Content-Type": "text/html"});
    response.write(body);
    response.end();
}


// triing to add a new User to database
function newUser(response, postData, db) {
  console.log("Request handler 'newUser' was called.");

	// adding to database

	var login = querystring.parse(postData).login;
	var password = querystring.parse(postData).password;

	db.exec("INSERT INTO users VALUES ('" + login + "' , '" + password + "');");


	var res = db.exec("SELECT * FROM users");
	for (var i = 0; i < res[0].values.length; i += 1) {
	    console.log(res[0].values[i]);
	}


  var succ_body = '<html>'+
    '<head>'+
    '<meta http-equiv="Content-Type" content="text/html; '+
    'charset=UTF-8" />'+
    '</head>'+
    '<body>'+
    '<h1>new user registered successfully!</h1>' +
    'use /send to send messages'
    '</body>'+
    '</html>';

    response.writeHead(200, {"Content-Type": "text/html"});
    response.write(succ_body);
    response.end();
}


// request for registering a new user
// after filling form redirects to /newUser
function register(response, PostData, db) {
  console.log("Request handler 'register' was called.");

  var body = '<html>'+
    '<head>'+
    '<meta http-equiv="Content-Type" content="text/html; '+
    'charset=UTF-8" />'+
    '</head>'+
    '<body>'+
    '<h1>sign up!</h1>'+
    '<form action="/newUser" method="post">'+
    '<textarea name="login" rows="1" cols="80" placeholder="login" rows="1" required></textarea>'+
    '<textarea name="password" rows="1" cols="80" placeholder="password" rows="1" required ></textarea>'+
    '<input type="submit" value="Submit text" />'+
    '</form>'+
    '</body>'+
    '</html>';

    response.writeHead(200, {"Content-Type": "text/html"});
    response.write(body);
    response.end();
}

// sening a message (just now it's simply written to database)
function send(response, postData, db) {
  console.log("Request handler 'send' was called.");

// adding to base

	var from = querystring.parse(postData).from;
	var to = querystring.parse(postData).to;
	var text = querystring.parse(postData).text;

	db.exec("INSERT INTO messages VALUES ('" + from + "' , '" + to + "' , '" + text + "');");


	var res = db.exec("SELECT * FROM messages");
	for (var i = 0; i < res[0].values.length; i += 1) {
	    console.log(res[0].values[i]);
	}


  var body = '<html>'+
    '<head>'+
    '<meta http-equiv="Content-Type" content="text/html; '+
    'charset=UTF-8" />'+
    '</head>'+
    '<body>'+
    '<h1>sending messages</h1>' +
    '<form action="/send" method="post">'+
    '<textarea name="from" rows="1" cols="80" placeholder="from" rows="1" required></textarea>'+
    '<textarea name="to" rows="1" cols="80" placeholder="to" rows="1" required></textarea>'+
    '<textarea name="text" rows="1" cols="80" placeholder="message..." rows="1" required></textarea>'+
    '<input type="submit" value="Submit text" />'+
    '</form>'+
    '</body>'+
    '</html>';

  response.writeHead(200, {"Content-Type": "text/html"});
  response.write(body);
  
  response.write("previous action: "+
	" FROM " + querystring.parse(postData).from +
	" TO " + querystring.parse(postData).to +
	" SEND " + querystring.parse(postData).text);
  response.end();
}


exports.register = register;
exports.send = send;
exports.hello = hello;
exports.newUser = newUser;
