// main file.
// starts the application


var server = require("./server");
var router = require("./router");
var requestHandlers = require("./requestHandlers");


// initialise event handlers
var handle = {}
handle["/"] = requestHandlers.hello;
handle["/send"] = requestHandlers.send;
handle["/register"] = requestHandlers.register;
handle["/newUser"] = requestHandlers.newUser;


var sql = require('sql.js');
var loader = require('./load_base.js');
var db = loader.loadDataBase();

server.start(router.route, handle, db);
