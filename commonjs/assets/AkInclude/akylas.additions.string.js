if (typeof(String.prototype.assign) === "undefined") {
    function fillAssign(_assign, _args, _prefix) {
        var prefix = _prefix ? (_prefix + '.') : '';
        _.each(_args, function(element, key, list) {
            if (_.isObject(element) && !_.isArray(element)) {
                fillAssign(_assign, element, key);
            } else _assign[_.isString(key) ? (prefix + key) : (key + 1)] =
                element;
        });
    }

    String.prototype.assign = function() {
        var assign = {};
        fillAssign(assign, arguments);
        return this.replace(/\{([^{]+?)\}/g, function(m, key) {
            return assign[key] || m;
        });
    };
}