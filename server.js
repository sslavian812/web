// server listening 8888


var http = require("http");
var url = require("url");

function start(route, handle, db) {
  function onRequest(request, response) {
    var postData = "";
    var pathname = url.parse(request.url).pathname;
    console.log("Request for " + pathname + " received.");

    request.setEncoding("utf8");

    request.addListener("data", function(postDataChunk) {
      postData += postDataChunk;
      console.log("Received POST data chunk '"+
      postDataChunk + "'.");
    });

    request.addListener("end", function() {
      route(handle, pathname, response, postData, db);
    });

  }

  http.createServer(onRequest).listen(8888);
  console.log('server is running on 8888');
}

exports.start = start;
