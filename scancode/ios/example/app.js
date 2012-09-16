
// open a single window
var win = Ti.UI.createWindow({
    backgroundColor:'white',
    orientationModes:[ Titanium.UI.PORTRAIT, Titanium.UI.UPSIDE_PORTRAIT, Titanium.UI.LANDSCAPE_LEFT, Titanium.UI.LANDSCAPE_RIGHT ]
});

// object to store last event position
var touchMoveBase = {
    set: function(point) {
        this.x = point.x;
        this.y = point.y;
    }
};


var cropRectView = Ti.UI.createView({
    width:200,
    height:200,
    left:40,
    top:80,
    backgroundColor:'#5500ff00',
    touchPassThrough:true
    // borderWidth:2,
    // borderColor:'white'
});

var shotPreview = Ti.UI.createImageView({
    width:300,
    height:300,
    preventDefaultImage:true,
    touchPassThrough:true,
    opacity:0.5,
    top:0
});

var cropRectRealView = Ti.UI.createView({
    backgroundColor:'#55ff0000',
    // borderWidth:2,
    touchPassThrough:true,
    // borderColor:'green',
    visible:false
});

// TODO: write your module tests here
var scancodeModule = require('akylas.scancode');
Ti.API.info("module is => " + scancodeModule);

var originalScanViewHeight = 400;
var scanView = scancodeModule.createView({
    readers:['qrcode', 'ean8', 'ean13'],
    backgroundColor:'black',
    // centeredCropRect:true,
    // height:originalScanViewHeight,
    centeredCropRect:false
});


win.addEventListener('postlayout', function(e){
    udpateCropRect();
});

win.addEventListener('touchstart', function(e) {
    Ti.API.info('test touchstart');
});

var olt = Titanium.UI.create2DMatrix(), curX, curY;
cropRectView.addEventListener('touchstart', function(e) {
    curX = e.x;
    curY = e.y;
});
cropRectView.addEventListener('touchmove', function(e) {
    var deltaX = e.x - curX, deltaY = e.y - curY;
    olt = olt.translate(deltaX, deltaY, 0);
    cropRectView.transform = olt;
});

cropRectView.addEventListener('touchend', function(e) {
    udpateCropRect();
});

function udpateCropRect()
{
    // if (scanView.centeredCropRect) return;
    Ti.API.info("cropRect to => " + JSON.stringify(cropRectView.rect));
    scanView.cropRect = cropRectView.rect;
    var center = {x:(cropRectView.rect.x + cropRectView.rect.width/2),
        y:(cropRectView.rect.y + cropRectView.rect.height/2)};
    scanView.autoFocus(center);
}


Ti.Gesture.addEventListener('orientationchange', function(e){
    udpateCropRect();
});

scanView.addEventListener('singletap', function(e)
{
    scanView.focus({x:e.x, y:e.y});
    Ti.API.info('autoFocus');
});

scanView.addEventListener('doubletap', function(e)
{
    var center = {x:(cropRectView.rect.x + cropRectView.rect.width/2),
        y:(cropRectView.rect.y + cropRectView.rect.height/2)};
    scanView.autoFocus(center);
    Ti.API.info('doubletap');
});

var currentScale = 1.0;
var lastScale = 1.0;
scanView.addEventListener('pinch', function(e){
   currentScale = e.scale;
   scanView.height = originalScanViewHeight*lastScale*currentScale;
   udpateCropRect();
   // var transform = Ti.UI.create2DMatrix().scale(1.0, lastScale*currentScale);
   // scanView.transform = transform;
   return 0;
});
 
scanView.addEventListener('touchend', function(e){
    lastScale = (lastScale * currentScale);
    currentScale = 1.0;
    Ti.API.debug("pinchend event occurred.");
}); 
 

var btnOneD = Ti.UI.createButton({
    width:100,
    height:50,
    top:10,
    left:10,
    title:'OneD'
});
btnOneD.addEventListener('singletap', function()
{
    scanView.readers = ['ean8', 'ean13'];
    var current = scanView.onlyOneDimension;
    Ti.API.info('onlyOneDimension is currently ' + current);
});

var btnQRCode = Ti.UI.createButton({
    width:100,
    height:50,
    top:10,
    title:'QrCode'
});
btnQRCode.addEventListener('singletap', function()
{
    scanView.readers = ['qrcode'];
    var current = scanView.onlyOneDimension;
    Ti.API.info('onlyOneDimension is currently ' + current);
});

var btnMultiD = Ti.UI.createButton({
    width:100,
    height:50,
    top:10,
    right:10,
    title:'multiD'
});
btnMultiD.addEventListener('singletap', function()
{
    scanView.readers = ['qrcode', 'ean8', 'ean13'];
    var current = scanView.onlyOneDimension;
    Ti.API.info('onlyOneDimension is currently ' + current);
});

var btnFlash = Ti.UI.createButton({
    width:100,
    height:50,
    bottom:10,
    left:10,
    title:'flash'
});

btnFlash.addEventListener('singletap', function()
{
    var current = scanView.torch;
    Ti.API.info('torch is currently ' + current);
    scanView.torch = !current;
});

var btnSwitchCam = Ti.UI.createButton({
    width:100,
    height:50,
    bottom:10,
    right:10,
    title:'Front/Rear'
});

btnSwitchCam.addEventListener('singletap', function()
{
    Ti.API.info('camera is currently ' + scanView.cameraPosition);
    scanView.swapCamera();
});

var pointViews = [];
for (var i = 3; i >= 0; i--) {
    pointViews[i] = Ti.UI.createView({
        width:15,
        height:15,
        backgroundColor:'red',
        // borderWidth:1,
        touchPassThrough:true,
        // borderColor:'green',
        visible:false
    });
    cropRectRealView.add(pointViews[i]);
}


function flush()
{
    for (var i = pointViews.length - 1; i >= 0; i--) {
        pointViews[i].visible = false;
    }
    cropRectRealView.visible = false;
    scanView.flush();
}

win.add(scanView);
win.add(btnOneD);
win.add(btnQRCode);
win.add(btnMultiD);
win.add(btnFlash);
win.add(btnSwitchCam);
scanView.add(cropRectView);
scanView.add(shotPreview);
scanView.add(cropRectRealView);

scanView.addEventListener('scan', function(e)
{
    Ti.API.info('scan: ' + JSON.stringify(e));
    var cropRect = e.cropRect;
    var points = e.points;

    shotPreview.width = cropRectRealView.width = cropRect.width;
    shotPreview.height = cropRectRealView.height = cropRect.height;
    shotPreview.image  =e.image;
    if (scanView.centeredCropRect === false)
    {
        shotPreview.left = cropRectRealView.left = cropRect.x;
        shotPreview.top = cropRectRealView.top = cropRect.y;
    }
    cropRectRealView.visible = true;
    for (var i = points.length - 1; i >= 0; i--) {
        pointViews[i].center = points[i];
        pointViews[i].visible = true;
    }

    setTimeout(flush, 3000);
});

scanView.addEventListener('start', function(e)
{
    Ti.API.info('start: ' + JSON.stringify(e));
});

scanView.addEventListener('stop', function(e)
{
    Ti.API.info('stop: ' + JSON.stringify(e));
});

win.open();
