function findDeep(obj, key) {
        
        // or efficient:
        var res = [];
        if (_.has(obj, key)) // or just (key in obj)
            res.push(obj);
        _.forEach(obj, function (v) {
            if (typeof v == "object" && (v = findDeep(v, key)).length) {
                res.push.apply(res, v);
            } else if (Array.isArray(v) && (v = findDeep(v, key)).length) {
                res.push.apply(res, v);
            }
        });
        return res;
    }

class TemplateModule {
    prepareTemplate(_template, _type?: string, _defaults?: Object) { }
    constructor() {
        this.prepareTemplate = ak.ti.style
    }
    internalAddEvents = (_template, _properties?) => {
        if (!_template || !_properties) return;
        var props;
        if (_properties.hasOwnProperty('events')) {
            props = _template.events || _template;
            _template.events = _template.events || {};
            Object.assignDeep(_template.events, _properties.events);
            delete _properties.events;
        }
        if (_template.bindId && _properties.hasOwnProperty(_template.bindId)) {
            _template.events = _template.events || {};
            Object.assignDeep(_template.events, _properties[_template.bindId]);
            delete _properties[_template.bindId];
            // delete _template.bindId;
        }
        if (Object.keys(_properties).length === 0) return true;
        _template.childTemplates && _template.childTemplates.forEach(function (ele, i, list) {
            return !this.internalAddEvents(ele, _properties);
        }, this);
    }
    internalAddProperties = (_template, _properties?) => {

        if (!_template || !_properties) return;
        // var props;
        if (_properties.hasOwnProperty('properties')) {
            var props = _template.properties || _template;
            Object.assignDeep(props, _properties.properties);
            delete _properties.properties;
        }

        var objs = findDeep(_template, 'bindId');
        objs.forEach(obj => {
            var binding = obj.bindId;
            if (_properties.hasOwnProperty(obj.bindId)) {
                // props = obj.properties || obj;
                Object.assignDeep(obj.properties || obj, _properties[binding]);
            }
        });
        // if (_template.bindId && _properties.hasOwnProperty(_template.bindId)) {
        //     props = _template.properties || _template;
        //     Object.assignDeep(props, _properties[_template.bindId]);
        //     delete _properties[_template.bindId];
        //     // delete _template.bindId;
        // }
        // if (Object.keys(_properties).length === 0) return true;
        // _template.childTemplates && _template.childTemplates.forEach(function (ele, i, list) {
        //     return !this.internalAddProperties(ele, _properties);
        // }, this);

    }
    addProperties = (_template, _properties?) => {
        if ((typeof _template === 'string')) {
            this.internalAddProperties(this.getTemplate(_template), _properties);
        } else {
            this.internalAddProperties(_template, _properties);
        }
    }

    cloneTemplate = (_template: string, _events?) => {
        var template = this.getTemplate(_template);
        if (template) {
            template = _.cloneDeep(template);
            if (_events) {
                _events.forEach(function (value, key, list) {
                    this.internalAddEvents(template, key, value);
                }, this);
            }
            return template;
        }
        return null;
    }

    

    cloneTemplateAndFill = (_template, _properties?, _events?) => {
        var template = (Object.isObject(_template)) ? _template : this.getTemplate(_template);
        // console.debug('cloneTemplateAndFill', _template, _properties);
        if (template) {
            template = _.cloneDeep(template);
            if (_properties) {
                this.internalAddProperties(template, _properties);
            }
            if (_events) {
                this.internalAddEvents(template, _events);
            }
            return template;
        }
        return null;
    }
    getTemplate = (_key: string): any => {
        return this[_key];
    }
    addTemplate = (_template, _key: string) => {
        this[_key] = this.prepareTemplate(_template);
    }
}
