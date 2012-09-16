// This is a test harness for your module
// You should do something interesting in this harness 
// to test out the module and to provide instructions 
// to users on how to use it by example.


// open a single window
var win = Ti.UI.createWindow({
	backgroundColor:'white'
});
var label = Ti.UI.createLabel();
win.add(label);
win.open();

// TODO: write your module tests here
var akylas_totali_ios = require('akylas.totali');
Ti.API.info("module is => " + akylas_totali_ios);

label.text = akylas_totali_ios.example();

Ti.API.info("module exampleProp is => " + akylas_totali_ios.exampleProp);
akylas_totali_ios.exampleProp = "This is a test value";

if (Ti.Platform.name == "android") {
	var proxy = akylas_totali_ios.createExample({
		message: "Creating an example Proxy",
		backgroundColor: "red",
		width: 100,
		height: 100,
		top: 100,
		left: 150
	});

	proxy.printMessage("Hello world!");
	proxy.message = "Hi world!.  It's me again.";
	proxy.printMessage("Hello world!");
	win.add(proxy);
}

