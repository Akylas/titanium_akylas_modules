// This is a test harness for your module
// You should do something interesting in this harness
// to test out the module and to provide instructions
// to users on how to use it by example.


// open a single window
var win = Ti.UI.createWindow({
	backgroundColor:'white'
});

// TODO: write your module tests here
var ak_test = require('akylas.commonjs');

ak_test.load(['ti', 'moment', 'lang', 'underscore', 'backbone', 'animation'], [], this);
ak.ti.reduxApplyDPUnitsToPixels(true);

var view = new View({
	backgroundColor:'red',
	width:100,
	height:100
});

win.add(view);
win.open();
