

ak.ti.constructors.createAppTabController = function(_args) {

    var self:AppTabController = new View(_args) as AppTabController;
    var tabRClass = _args.rclassTab || 'AppTab';
    self.add(ak.ti.style({
        type: 'Ti.UI.View',
        bindId: 'container',
        properties: {
            rclass: 'AppTabControllerContainer'
        }
    }), 0);
    var currentTab = 0;

    function prepareTabs(_tabs) {
        var currentCount = self ? self.container.children.length : 0;
        var selected = currentCount === 0;
        var tabsToAdd = [];
        for (var i = 0; i < _tabs.length; i++) {
            tabsToAdd.push({
                type: 'Ti.UI.Label',
                properties: {
                    text: _tabs[i],
                    index: currentCount,
                    rclass: tabRClass,
                    enabled: !selected
                }
            });
            currentCount++;
            selected = false;
        }
        return tabsToAdd;
    }
    if (_args.labels) {
        ak.ti.add(self.container, prepareTabs(_args.labels));
    }

    self.setLabels = function(_tabs) {
        self.removeAllChildren();
        self.add(prepareTabs(_tabs));
    };

    self.addTab = function(_title) {
        self.add(prepareTabs([_title]));
    };

    self.on('click', function(_event) {
        if (self.containsView(_event.source)) {
            self.setIndex(_event.source.index);
            self.fireEvent('request_tab', {
                index: _event.source.index
            });
        }

    });

    self.setIndex = function(_index) {
        if (_index !== undefined && _index !== currentTab) {
            var children = self.container.children;
            children[currentTab].enabled = true;
            children[_index].enabled = false;
            currentTab = _index;
        }
    };

    //END OF CLASS. NOW GC 
    self.GC = app.composeFunc(self.GC, function() {
        self = null;
    });
    return self;
};