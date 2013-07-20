

var Image = require('akylas.image');

var win = Titanium.UI.createWindow({
    backgroundColor: 'red',
    title: 'Red Window'
});



var button = Titanium.UI.createButton({
    title: 'Open Blue Window'
});
button.addEventListener('click', function(){
	win.toImage(function(_event){
		var win3 = Titanium.UI.createWindow({
			backgroundImage:Image.getFilteredImage(_event.image, Image.FILTER_GAUSSIAN_BLUR, {blurSize:1.0}),
		    title: 'Blue Window'
		});
	    win3.open({
	    	activityEnterAnimation: Ti.Android.R.anim.fade_in,
			activityExitAnimation: Ti.Android.R.anim.fade_out
	    });
	}, 0.5);

});

win.add(button);
win.open();