var __notifWin = null;
var __notifLabel = null;
__notifWin_closeTimer = null;

ak.ti.constructors.createNotification = function(_args)
{
	var self = _args;

	if (__notifWin === null) {
		__notifWin = new Window({rclass:'NotificationWindow'});
		__notifLabel = new Label({rclass:'NotificationMessageLabel'});
		__notifWin.add(__notifLabel);
	}

	self.show = function(){
		__notifLabel.text = self.message|| 'hello';
		var needsOpen = (__notifWin_closeTimer === null);

		if (__notifWin_closeTimer !== null)
		{
			clearTimeout(__notifWin_closeTimer);
			__notifWin_closeTimer = null;
		}
		setTimeout(function()
		{
			__notifWin.close({opacity:0,duration:500});
		},(self.duration || 2000));
		if (needsOpen)
		{
			__notifWin.opacity = 0.0;
			__notifWin.open({opacity:1.0,duration:200});
		}
	}

	self.close = function()
	{
		if (__notifWin_closeTimer !== null)
		{
			__notifWin.close({opacity:0.0,duration:500});
			clearTimeout(__notifWin_closeTimer);
			__notifWin_closeTimer = null;
		}
	}
    return self;
}