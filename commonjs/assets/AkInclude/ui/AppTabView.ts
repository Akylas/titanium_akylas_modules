

var constructors = ['Ti.UI.createScrollableView'];
ak.ti.constructors.createAppTabView = function (_args) {
    var tabs = _args.tabs;
    var currentPage = _args.currentPage || 0;
    var showControls = _.remove(_args, 'showControls', true);
    var createTab = _.remove(_args, 'createTab');
    var nativeControls = _.remove(_args, 'nativeControls', false);
    var tabsControllerClass = _args.tabsControllerClass;
    var pagerClass = _args.pagerClass || 'AppTabViewScrollableView';
    var loadedTabs = [];
    delete _args.tabs;
    _args = {
        properties: _args,
        childTemplates: []
    };

    var tabController:AppTabController;

    _args.childTemplates.push({
        type: 'Ti.UI.ScrollableView',
        bindId: 'pager',
        properties: {
            rclass: pagerClass,
            views: tabs
        },
        'events': {
            scrollend: function (e) {
                if (e.hasOwnProperty('currentPage')) {
                    if (tabController) {
                        tabController.setIndex(e.currentPage);
                    }
                    var oldTab = tabs[currentPage],
                        newTab = e.view;
                    currentPage = e.currentPage;
                    e.oldView = oldTab;
                    if (oldTab && oldTab.blur) {
                        oldTab.blur();
                    }
                    if (newTab) {
                        if (newTab && newTab.focus) {
                            newTab.focus();
                        }

                    }
                    self.emit('change', e);
                }

            },
            change: function (e) {
                // console.debug('change', e);
                self.currentView = e.view;
                if (loadedTabs.indexOf(e.currentPage) !== -1) {
                    loadedTabs.push(e.currentPage);
                    self.currentView.emit('first_load');
                }
                self.emit('change', e);
            }
        }
    });
    if (showControls !== false) {
        var titles = _.map(tabs, 'title');
        if (nativeControls === true) {
            if (__APPLE__) {
                tabController = new ButtonBar({
                    bindId: 'buttonbar',
                    index: 0,
                    rclass: tabsControllerClass,
                    labels: titles
                }) as AppTabController;
                tabController.on('click', function (_event) {
                    self.setTab(_event.index);
                });
            } else {
                _args.childTemplates[0].properties.strip = ak.ti.style({
                    titles: titles,
                    rclass: tabsControllerClass,
                });
            }

        } else {
            tabController = new AppTabController({
                rclass: tabsControllerClass,
                createTab:createTab,
                labels: titles
            });
            tabController.addEventListener('request_tab', function (_event) {
                self.setTab(_event.index);
            });
        }

        if (tabController) {
            _args.childTemplates.push(tabController);
        }

    }

    var self: AppTabView = Object.assign(new View(_args), {
        setTab: function (_index) {
            console.log('setTab', _index, currentPage);
            if (currentPage != _index) {
                self.pager.scrollToView(_index);
            } else {
                self.fireEvent('tab_should_go_back', {index:_index, view:self.pager.views[_index]});
            }
        },
        setTabs: function (_tabs) {
            tabs = _tabs;
            self.pager.views = tabs;
            if (tabController) {
                tabController.setLabels(_.map(tabs, 'title'));
            }
        },
        getTab: function (_index) {
            return tabs[_index];
        },
        getTabs: function () {
            return tabs;
        },
        moveNext: function () {
            self.pager.moveNext();
        }
    }) as AppTabView;

    self.setTab(currentPage);

    //END OF CLASS. NOW GC
    self.GC = app.composeFunc(self.GC, function () {
        if (tabController && tabController.GC) {
            tabController.GC();
        }
        self.currentView = null;
        tabController = null;
        if (tabs && tabs !== null) {
            _.each(tabs, function (value, key, list) {
                if (value.GC) {
                    value.GC();
                }
            });
            tabs = null;
        }
        self = null;
    });
    return self;
};