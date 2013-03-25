var __myPath = Ti.resourcesRelativePath;
if (__myPath.length > 0 && __myPath[__myPath.length - 1] != '/')
	__myPath += '/';
Ti.API.info('__myPath in commonjs ' + __myPath);

exports.load = function(_modules, _additions, _context) {
	// this = _context;
	(function(){
		var akadditions = _additions;
		var akmodules = _modules;
		Ti.include(__myPath  +'AkInclude/akylas.global.js');
		delete akadditions;
		delete akmodules;
	}).call(_context);
};