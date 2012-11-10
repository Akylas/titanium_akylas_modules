// This is a test harness for your module
// You should do something interesting in this harness 
// to test out the module and to provide instructions 
// to users on how to use it by example.

// TODO: write your module tests here
var module = require('akylas.udp');
var udpSocket = module.createSocket();
udpSocket.addEventListener('data', function (evt) {
        var string = evt['stringData'];
        var json = JSON.parse(string);
        // if (json.hasOwnProperty('tp') && json.hasOwnProperty('data')) 
        // {
            // var type = json['tp'];
            // var data = json['data'];
//  
            // Ti.App.fireEvent('servermsg:' + type, {data:data});
        // }
	});
udpSocket.start({
	port: 10001
});

// open a single window
var win = Ti.UI.createWindow({
	backgroundColor:'white'
});
var label = Ti.UI.createLabel();
win.add(label);
win.open();

