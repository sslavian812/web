// loads database from file in the same directory

exports.loadDataBase = function() {
	var fs = require('fs');
	var SQL = require('sql.js');

	var filebuffer = fs.readFileSync('./chat_v_0_0_1.sqlite');
	var db = new SQL.Database(filebuffer);
	return db;
}

