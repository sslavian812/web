// DO NOT START WITHOUT REASON!!!
// only manually

var fs = require("fs");
var sql = require('sql.js');

// Create a database
var db = new sql.Database();

sqlstr = "CREATE TABLE users (_name char, _password char);";
db.run(sqlstr);
sqlstr = "CREATE TABLE messages (_from char, _to char, _text char);";
db.run(sqlstr);

var data = db.export();
var buffer = new Buffer(data);
fs.writeFileSync("chat_v_0_0_1.sqlite", buffer);
