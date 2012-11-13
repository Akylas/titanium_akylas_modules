// This is a test harness for your module
// You should do something interesting in this harness 
// to test out the module and to provide instructions 
// to users on how to use it by example.


// open a single window
var win = Ti.UI.createWindow({
	backgroundColor:'white',
		orientationModes : [Titanium.UI.LANDSCAPE_LEFT, Titanium.UI.LANDSCAPE_RIGHT, Titanium.UI.PORTRAIT, Titanium.UI.UPSIDE_PORTRAIT]
});
var akylas_camera_android = require('akylas.camera');
var cameraview = akylas_camera_android.createView({
	cameraPosition:akylas_camera_android.CAMERA_FRONT,
	quality:akylas_camera_android.QUALITY_HIGH,
	torch:true
});

cameraview.addEventListener('singletap', function(e)
{
    cameraview.focus({x:e.x, y:e.y});
    Ti.API.info('focus');
});

cameraview.addEventListener('swipe', function(e)
{
	if (e.direction === 'left')
		cameraview.swapCamera();
	else if (e.direction === 'up')
		cameraview.torch = true;
	else if (e.direction === 'down')
		cameraview.torch = false;
});

cameraview.addEventListener('doubletap', function(e)
{
    var center = {x:(cameraview.rect.x + cameraview.rect.width/2),
        y:(cameraview.rect.y + cameraview.rect.height/2)};
    cameraview.autoFocus(center);
    Ti.API.info('autoFocus');
});
win.add(cameraview);
win.open();
