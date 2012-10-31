// This is a test harness for your module
// You should do something interesting in this harness 
// to test out the module and to provide instructions 
// to users on how to use it by example.


// open a single window
var win = Ti.UI.createWindow({
	backgroundColor:'white'
});
var akylas_camera_android = require('akylas.camera');
var cameraview = akylas_camera_android.createView();
win.add(cameraview);
win.open();
