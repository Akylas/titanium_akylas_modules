// const separator = Ti.Filesystem.getSeparator()
export class AKLang implements AK.IAKLang {
    defaultLanguage = 'en-US'
    currentLanguage = 'en-US'
    private jsonLang = {}
    private storeMissingTranslations = false
    missings = {}
    private missingsFile = null
    availableLanguages: string[] = []
    useI18next = false
    i18n
    constructor(_context, _config) {
        _context.loadLanguage = this.loadLanguage;
        _context.tr = this.tr;
        _context.trc = this.trc;
        _context.trt = this.trt;
        if (_config && _config.hasOwnProperty('defaultLanguage')) {
            this.defaultLanguage = _config.defaultLanguage;
        }
        if (_config.i18next === true) {
            this.useI18next = true;
            var path = _config.modulesDir;
            console.debug('loading i18next', path);
            this.i18n = require(path + 'i18next');
            if (this.i18n) {
                console.debug('i18nnext loaded');
                _context.t = function(_id, _params) {
                    return this.i18n.t(_id, Object.assign({
                        defaultValue: _id
                    }, _params));
                };
            }
        }
        var dir = Ti.Filesystem.getFile(Ti.Filesystem.resourcesDirectory, 'lang');
        if (dir.exists()) {
            var dir_files = dir.getDirectoryListing();
            if (dir_files) {
                if (this.useI18next) {
                    this.availableLanguages = dir_files;
                } else {
                    var reg = /(.*)\.json/,
                        match;
                    this.availableLanguages = dir_files.map(function(value: string) {
                        match = value.match(reg);
                        if (match && match.length > 1) {
                            return match[1];
                        }
                    })
                }

            }
        }
        console.debug('locale', this, _config);
        var loadModuleLang = (function(moduleName, path, callback, ...langs) {
            langs.forEach(function(value: string) {
                var lang = null;
                try {
                    lang = require(path + 'lang/' + value);
                    if (lang === null) {
                        lang = require('/' + moduleName + '/lang/' + value);
                    }
                } catch (e) { }

                if (lang !== null) {
                    callback(value, lang);
                } else {
                    console.debug('could not load moment lang:', value);
                }
            });
        }).bind(_context);

        if (_context.moment) {
            const moment = _context.moment;
            _context.moment.loadLangs = (lang) => {
                loadModuleLang('moment', _config.momentPath, function(id, value) {
                    moment.locale(id, value);
                    moment.locale(id);
                }, lang);
            }
            if (_context.numeral) {
                const numeral = _context.numeral;
                _context.numeral.loadLangs = (lang) => {
                    loadModuleLang('numeral', _config.numeralPath, function(id, value) {
                        numeral.language(id, value);
                        numeral.language(id);
                    }, lang);
                }
            }
        }
    }
    loadLanguage = (_context, _lang?: string) => {
        var canLoadDefault = _lang === undefined;
        var id = _lang || Titanium.Locale.currentLanguage,
            langFile, file;
        if (this.availableLanguages.indexOf(id) !== -1) {
            langFile = id;
        } else {
            id = id.split('-')[0];
            if (this.availableLanguages.indexOf(id) !== -1) {
                langFile = id;
            }
        }
        console.debug('loadLanguage', _lang, langFile, this.availableLanguages);
        if (!langFile && canLoadDefault) {
            console.debug('can\'t load language', id);
            if (_lang !== this.defaultLanguage && this.currentLanguage !== this.defaultLanguage) {
                console.debug('trying default', this.defaultLanguage);
                this.loadLanguage(_context, this.defaultLanguage);
            }
            return;
        }

        // if (this.useI18next) {
        //     var dir = Ti.Filesystem.getFile(Ti.Filesystem.resourcesDirectory, 'lang', langFile);
        //     if (dir.exists()) { // file is found
        //         langFile = this.defaultLanguage;
        //         dir = Ti.Filesystem.getFile(Ti.Filesystem.resourcesDirectory, 'lang', langFile);
        //         var dir_files = dir.getDirectoryListing();
        //         if (!dir_files || dir_files === null)
        //             return;
        //         for (var i = 0; i < dir_files.length; i++) {
        //             var dirFile = dir_files[i];
        //             if (!_.endsWith(dirFile, '.json'))
        //                 continue;
        //             file = Ti.Filesystem.getFile(dir.nativePath, path);
        //             if (file.exists()) {
        //                 console.debug('addResourceBundle', langFile, dirFile.replace('.json', ''));
        //                 this.i18n.addResourceBundle(langFile, dirFile.replace('.json', ''), eval.call(_context || this, '(' + file.read()
        //                     .text +
        //                     ')'));
        //             }
        //         }
        //     }
        // } else {
        file = Ti.Filesystem.getFile(Ti.Filesystem.resourcesDirectory, 'lang', langFile + '.json');
        if (file.exists()) {
            this.jsonLang = eval.call(_context || this, '(' + file.read().text + ')');
        }
        // }

        // grab it & use it
        this.currentLanguage = langFile || id;
        if (!!this.storeMissingTranslations) {
            this.missings = {};
            this.missingsFile = Ti.Filesystem.getFile(Ti.Filesystem.applicationDataDirectory, 'lang_' + this.currentLanguage + '_missing.json');

        }
        if (_context.moment) {
            try {
                console.debug('loading moment lang', this.currentLanguage);
                _context.moment.loadLangs(this.currentLanguage);
            } catch (e) {
                console.debug('loadLanguage moment error', JSON.stringify(e));
            }
        }
        if (_context.numeral) {
            try {
                console.debug('loading numeral lang', this.currentLanguage);
                _context.numeral.loadLangs(this.currentLanguage);
            } catch (e) {
                console.debug('loadLanguage numeral error', JSON.stringify(e));
            }
        }

    }

    appendLanguage(_context, _data: Object | string) {
        try {
            if (_data.hasOwnProperty) {
                Object.assign(this.jsonLang, _data);
            } else {
                var file = Ti.Filesystem.getFile(Ti.Filesystem.resourcesDirectory, <string>_data, 'lang', this.currentLanguage + '.json');
                if (file.exists()) {
                    var data = eval.call(_context || this, '(' + file.read().text + ')');
                    for (var key in data) {
                        this.jsonLang[key] = data[key];
                    }
                }
            }

        } catch (e) {
            console.error('appendLanguage error', e);
        }
    }
    tr = (_id: string, _default?: string): string => {
        _default = _default || _id;
        if (this.jsonLang.hasOwnProperty(_id))
            return this.jsonLang[_id];
        else {
            if (!!this.storeMissingTranslations) {
                console.debug('missing translation', _id);
                if (!this.missings.hasOwnProperty(_id)) {
                    console.debug('adding missing translation', _id);
                    this.missings[_id] = '';
                    if (this.missingsFile) {
                        console.debug('saving missing translations');
                        this.missingsFile.write(JSON.stringify(this.missings, null, 4));
                    }
                }
            }
            return _default;
        }
    }
    trt = (_id: string, _default?: string): string => {
        let str = this.tr(_id, _default);
        if (!str) {
            return _id;
        }
        var array = str.split(/[\s_]+/).map(function(word: string) {
            return word.charAt(0).toUpperCase() + word.slice(1);
        });
        return array.join(' ');
    };
    trc = (_id: string, _default?: string): string => {
        let str = this.tr(_id, _default);
        if (!str) {
            return _id;
        }
        str = str.split(/[\s_]+/).join(' ');
        return str.charAt(0).toUpperCase() + str.slice(1);
    }
}

export function init(context, option) {
    return new AKLang(context, option);
}
