var __myPath = Ti.resourcesRelativePath;
if (__myPath.length > 0 && __myPath[__myPath.length - 1] != '/')
	__myPath += '/';
Ti.API.info('__myPath in commonjs ' + __myPath);

exports.load = function(_modules, _additions, _context) {
	_context.akadditions = _additions;
	_context.akmodules = _modules;
	_context.ak = require(__myPath  +'AkInclude/akylas.global').init(_context);
};

exports.loadExtraWidgets = function(_context) {
	if (_context.ak.ti){
		var isApple =  Ti.Platform.osname === 'ipad' || Ti.Platform.osname === 'iphone';
		if (isApple) {

			(function(){
				ak.ti.loadCreators(__myPath  +'AkWidgets/Notification.js');
			}).call(_context);
		}
	}
};