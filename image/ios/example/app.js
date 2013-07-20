

var Image = require('akylas.image');

var win1 = Titanium.UI.createWindow();

var win2 = Titanium.UI.createWindow({
    backgroundColor: 'red',
    title: 'Red Window'
});



var button = Titanium.UI.createButton({
    title: 'Open Blue Window'
});
button.addEventListener('click', function(){
	win1.toImage(function(_event){
		var win3 = Titanium.UI.createWindow({
			backgroundImage:Image.getFilteredImage(_event.image, Image.FILTER_GAUSSIAN_BLUR, {blurSize:1.0}),
		    title: 'Blue Window'
		});
	    win3.open({
	    	transition: Titanium.UI.iPhone.AnimationStyle.CROSS_DISSOLVE,
	    	duration:400
	    });
	}, 0.5);
	
});

var nav = Titanium.UI.iPhone.createNavigationGroup({
   window: win2
});

win2.add(button);
win1.add(nav);
win1.open();