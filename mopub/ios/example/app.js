var params;
if (Titanium.Platform.name == 'android') {
	params = {
		siteId: 35176,
		siteUrl: "http://mobile.smartadserver.com",
		bannerId: 15140,
		bannerPageId: '(news_activity)',
		interstitialId: 12160,
		interstitialPageId: 185330
	}
} else {
	params = {
		siteId: 27893,
		siteUrl: "http://mobile.smartadserver.com",
		bannerId: 12161,
		bannerPageId: 185330,
		interstitialId: 12160,
		interstitialPageId: 185330
	}
}

var win = Ti.UI.createWindow({
	orientationModes: [Ti.UI.UPSIDE_PORTRAIT,
		Ti.UI.PORTRAIT,
		Ti.UI.LANDSCAPE_RIGHT,
		Ti.UI.LANDSCAPE_LEFT
	]
});
var events = ["error", "load", "action", "dismiss", "close", "resize", "showModal", "closeModal", "expand", "collapse"];

var sasmodule = require("akylas.itinerarium.sas");

sasmodule.setSiteIdAndBaseUrl(params.siteId, params.siteUrl);
/*
Banner View
*/

var bannerView = sasmodule.createBannerView({
	height: 50,
	width: Ti.UI.FILL,
	left: 0,
	bottom: 0,
	useSpinner: true,
	hideStatusBar: false
});
bannerView.loadFormatIdAndPageId({
	formatId: params.bannerId,
	pageId: params.bannerPageId,
	master: false,
	timeout: 3000
});

function onBannerEvent(e) {
	Ti.API.info('bannerView ' + JSON.stringify(e));
}

for (var i = 0; i < events.length; i++) {
	bannerView.addEventListener(events[i], onBannerEvent);
};

bannerView.addEventListener("data", function(e) {
	onBannerEvent(e);
	bannerView.height = e.data.imageSize.height;
	//maybe the banner changes its original height
});

win.add(bannerView);

var winInterstitial = Ti.UI.createWindow({
	backgroundColor: 'blue',
	orientationModes: [Ti.UI.UPSIDE_PORTRAIT,
		Ti.UI.PORTRAIT,
		Ti.UI.LANDSCAPE_RIGHT,
		Ti.UI.LANDSCAPE_LEFT
	]
});
var interstitialView = sasmodule.createInterstitialView({
	width: Ti.UI.FILL,
	height: Ti.UI.FILL
});

interstitialView.loadFormatIdAndPageId({
	formatId: params.interstitialId,
	pageId: params.interstitialPageId,
	master: false,
	timeout: 3000
});

function onInterstitialEvent(e) {
	if (e.type === 'dismiss') {
		winInterstitial.close();
	}
	Ti.API.info('interstitialView ' + JSON.stringify(e));
}

for (var i = 0; i < events.length; i++) {
	interstitialView.addEventListener(events[i], onInterstitialEvent);
};

interstitialView.addEventListener("data", function(e) {
	onInterstitialEvent(e);
	// bannerView.height = e.data.imageSize.height;
	//maybe the banner changes its original height
});
winInterstitial.add(interstitialView);
win.open();
winInterstitial.open();