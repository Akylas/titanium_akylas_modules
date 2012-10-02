// This is a test harness for your module
// You should do something interesting in this harness
// to test out the module and to provide instructions
// to users on how to use it by example.

// open a single window
var win = Ti.UI.createWindow({
    orientationModes:[Titanium.UI.PORTRAIT],
    backgroundColor : 'white',
    exitOnClose:true
});

// TODO: write your module tests here
var akylas_totali = require('akylas.totali');
Ti.API.info("module is => " + akylas_totali);



var totaliView = akylas_totali.createView({
    left:0,
    right:0,
    top:0,
    bottom:0,
    scenario : 'Scenario/project.dpd',
    backgroundColor:'blue'
});
win.add(totaliView);

var view = Ti.UI.createView({
    backgroundColor:'black',
    bottom:0,
    height:30
});

var orientationLabel = Ti.UI.createLabel({
    left:0,
    width:'50%'
});

var quaternionLabel = Ti.UI.createLabel({
    right:0,
    width:'50%'
});
view.add(orientationLabel);
view.add(quaternionLabel);
win.add(view);
// win.add(Ti.UI.createView({backgroundColor:'green', width:100, height:100, bottom:10}));
win.open();

totaliView.addEventListener("trackingChanged", function(e){
    orientationLabel.text = 'Orientation: ' + e.data[0];
    quaternionLabel.text = 'Quaternion: ' + e.data[1];
    // Ti.API.info('trackingChanged: ' + JSON.stringify(e));
});

totaliView.start();
totaliView.registerCallback("trackingChanged");
