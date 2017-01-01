function prepareModalOpeningArgs(_args) {
    _args = _args || {};
    if (!_args.hasOwnProperty('activityEnterAnimation'))
        _args.activityEnterAnimation = Ti.App.Android.R.anim.push_up_in;
    if (!_args.hasOwnProperty('activityExitAnimation'))
        _args.activityExitAnimation = Ti.App.Android.R.anim.identity;
    return _args;
}

function prepareModalClosingArgs(_args) {
    _args = _args || {};
    if (!_args.hasOwnProperty('activityEnterAnimation'))
        _args.activityEnterAnimation = Ti.App.Android.R.anim.identity;
    if (!_args.hasOwnProperty('activityExitAnimation'))
        _args.activityExitAnimation = Ti.App.Android.R.anim.push_down_out;
    return _args;
}

var isAndroid = Ti.Platform.osname === 'android';
class WindowManager implements IWindowManager {
    rootWindow: Ti.UI.Window
    private _winId = 0
    private _managers = {}
    // openedWindows: [],
    private TAG: 'WindowManager'
    handlingOpening = false
    defaultWinOpeningArgs = {}
    shouldDelayOpening = false
    androidNav = false
    title = 'WINDOW_MANAGER'
    _onWindowOpenedDelayed: Function
    constructor(_args) {

        this._onWindowOpenedDelayed = debounce(this._onWindowOpened, 400);
        this.shouldDelayOpening = _args.shouldDelayOpening !== false;
        this.androidNav = isAndroid && _args.androidNav === true;
        if (_args.winOpeningArgs) {
            this.defaultWinOpeningArgs = _args.winOpeningArgs;
        }
    }
    _onBack = (_win, e) => {
        if (_win.handleClose === true && _win.hideMe) {
            _win.hideMe();
        } else {
            this.closeWindow(_win);
        }
    }

    _onWindowOpened = (e) => {
        //if the window handles its open animation let's it set the variable
        //true later as it might be too soon now
        if (e.source.handleOpen !== true) {
            this.windowSignalsOpened(e.source);
        }
        // e.source.removeEventListener('focus', this._onWindowOpenedDelayed);
        // e.source.removeEventListener('open', this._onWindowOpened);
    }

    _onWindowClosed = (e) => {

        var win = e.source;

        if (win.onClose) {
            win.onClose();
        }

        if (e._closeFromActivityForcedToDestroy == true) {
            return;
        }

        var winManager = win.winManager || this;
        win.winManager = null;
        delete this._managers[win.winId];
        console.debug('_onWindowClosed', win.title);

        if (win.GC && (!win.manager || !win.manager.canGCWindow || win.manager.canGCWindow(win))) {
            win.manager = null;
            win.onBack = null;
            win.akmanaged = false;
            console.debug('GC Window:', win.title);

            win.GC();
        }
        // win.removeEventListener('close', this._onWindowClosed);
    }

    _openWindow = (_win, _args) => {
        var winManager = _win.winManager || this;

        if (_win.akmanaged !== true) {
            _win.winId = this._winId++;
        }
        this._managers[_win.winId] = winManager;

        // winManager.openedWindows = [_win].concat(winManager.openedWindows);

        // if (_win.cannotBeTopWindow !== true && winManager === this) {
        //     this.topWindow = _win;
        // }
        if (isAndroid) {
            if (_win.modal === true) {
                _win.winOpeningArgs = prepareModalOpeningArgs(_win.winOpeningArgs);
                _win.winClosingArgs = prepareModalClosingArgs(_win.winClosingArgs);
            }
            if (_win.akmanaged !== true) {
                if (!_win.onBack) {
                    _win.onBack = (e) => {
                        this._onBack(_win, e);
                    }
                }
                _win.addEventListener('androidback', _win.onBack);
            }
        }
        var realArgs = Object.assign(_win.winOpeningArgs ? _win.winOpeningArgs : ((_win.handleOpen === true) ? {
            animated: false
        } : {}), _args);

        console.debug('_openWindow', _win.title, _win.akmanaged);
        _win.akmanaged = true;
        // console.debug('_openWindow', realArgs);
        if (_win.winManager) {
            _win.winManager.openWindow(_win, realArgs);
        } else {
            // console.debug(this.TAG, '_openWindow', realArgs);
            _win.open(realArgs);
        }
        if (_win.toDoAfterOpening) {
            _win.toDoAfterOpening();
        }
    };

    createAndOpenWindow = (_constructor: string, _args, _openingArgs, _dontCheckOpening: Boolean) => {
        _args = _args || {};
        var winManager = _args.winManager || this;
        _dontCheckOpening = _dontCheckOpening || this.shouldDelayOpening === false;
        if (_dontCheckOpening !== true) {
            if (winManager.handlingOpening === true) {
                serror('Can\'t open window ' + _args.title);
                return;
            }
            winManager.handlingOpening = true;
        }

        var win = ak.ti.createFromConstructor(_constructor, _args);
        if (win.showMe && !win._opened) {
            win.showMe();
        } else {
            this.openWindow(win, Object.assign({
                winManager: _args.winManager
            }, _openingArgs), _dontCheckOpening);
        }
    }

    openWindow = (_win, _args?, _dontCheckOpening?: Boolean) => {
        _args = _args || {};
        _dontCheckOpening = _dontCheckOpening || this.shouldDelayOpening === false;
        if (_args.hasOwnProperty('winManager')) {
            _win.winManager = _args.winManager;
            delete _args.winManager;
        }

        var winManager = _win.winManager || this;
        console.debug('openWindow', _win.title, 'winManager:', this.title);
        var callback = _args.callback;
        delete _args.callback;
        // if (_.first(winManager.openedWindows) === _win) {
        //     if (callback) {
        //         callback(_win);
        //     }
        //     if (_win.toDoAfterOpening) {
        //         _win.toDoAfterOpening();
        //     }
        //     return;
        // }
        if (_dontCheckOpening !== true) {
            winManager.handlingOpening = true;
            //focus is sent before window animation starts and so a double click could try to open 2 windows.
            //delaying the focus event prevent that
            // _win.addEventListener('focus', this._onWindowOpenedDelayed);
            // _win.addEventListener('open', this._onWindowOpened);
            _win.once('open', this._onWindowOpenedDelayed);
            // ak.ti.listenOnce('open', this._onWindowOpenedDelayed, _win);
        }
        _win.once('close', this._onWindowClosed);
        // ak.ti.listenOnce('close', this._onWindowClosed, _win);

        if (callback) {
            callback(_win);
        }
        this._openWindow(_win, _args);
    }

    closeWindow = (_win, _args?, _callGC?: Boolean) => {
        if (!_win || _win === null)
            return;
        console.debug(_win.title, 'closeWindow1', _args, _win.hideMe, _win._closing);
        _args = _args || {};
        if (_win.hideMe && !_win._closing) {
            _win.hideMe();
        } else {
            var realArgs = Object.keys(_args).length > 0 ? _args : (_win.winClosingArgs || _win.winOpeningArgs || ((_win.handleClose === true) ? {
                animated: false
            } : this.defaultWinOpeningArgs));
            console.debug(_win.title, 'closeWindow', realArgs);
            _win.close(realArgs);
        }
    }

    windowSignalsOpened = (_win) => {
        var manager = this.getWindowManager(_win);
        if (manager)
            manager.handlingOpening = false;
    }
    getWindowManager = (_win) => {
        return this._managers[_win.winId];
    }
}

export = WindowManager;
