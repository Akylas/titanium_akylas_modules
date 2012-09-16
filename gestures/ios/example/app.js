// This is a test harness for your module
// You should do something interesting in this harness 
// to test out the module and to provide instructions 
// to users on how to use it by example.


// open a single window
var window = Ti.UI.createWindow({
	backgroundColor:'white'
});
var mainView = Ti.UI.createView();
window.add(mainView);

mainView.addGesture({
        name:"swipeLeftEvent",
        type:"swipe",
        directon:'left'
});

mainView.addGesture({
        name:"panEvent",
        type:"pan",
        mintouches:2,
        maxtouches:3
});


mainView.addEventListener('swipeLeftEvent', function(e){
    Ti.API.info('got swipe left in state: ' + e.state);
});

mainView.addEventListener('panEvent', function(e){
    Ti.API.info('got pan: touches(' + e.nbtouches + ') ,translation(' + e.translation.x + ', ' + e.translation.y + ')' + ' velocity(' + e.velocity.x + ', ' + e.velocity.y + ')');
});

window.open();


