

var Image = require('akylas.image');

var win = Titanium.UI.createWindow({
    backgroundColor: 'red',
    title: 'Red Window'
});



var button = Titanium.UI.createButton({
    title: 'Open Blue Window'
});
button.addEventListener('click', function(){
		var win3 = Titanium.UI.createWindow({
			backgroundImage:Image.getFilteredViewToImage(win, 0.5, Image.FILTER_GAUSSIAN_BLUR),
		    title: 'Blue Window'
		});
	    win3.open({
	    	activityEnterAnimation: Ti.Android.R.anim.fade_in,
			activityExitAnimation: Ti.Android.R.anim.fade_out
	    });

});

win.add(button);
win.open();