
/// No Circular
let oldStringify = JSON.stringify;
JSON.stringify = function (obj, replacer?, spaces?, cycleReplacer?) {
    return oldStringify(obj, serializer(replacer, cycleReplacer), spaces)
}

function serializer(replacer, cycleReplacer) {
    var stack = [], keys = []

    if (cycleReplacer == null) cycleReplacer = function (key, value) {
        if (stack[0] === value) return "[Circular ~]"
        return "[Circular ~." + keys.slice(0, stack.indexOf(value)).join(".") + "]"
    }

    return function (key, value) {
        if (stack.length > 0) {
            var thisPos = stack.indexOf(this)
            ~thisPos ? stack.splice(thisPos + 1) : stack.push(this)
            ~thisPos ? keys.splice(thisPos, Infinity, key) : keys.push(key)
            if (~stack.indexOf(value)) value = cycleReplacer.call(this, key, value)
        }
        else stack.push(value)

        return replacer == null ? value : replacer.call(this, key, value)
    }
}

function createString(str, count) {
    var array = [];
    for(var i = 0; i < count;)
        array[i++] = str;
    return array.join('');
}

function prettyStringify(obj, options) {
    options = options || {}
    var indent = get(options, 'indent', 2);
    var maxLength = (indent === '' ? Infinity : get(options, 'maxLength', 80));
    var maxDepth = get(options, 'maxDepth', 10);
    let seen = [];

    return (function _stringify(obj, currentDepth:number, reserved) {
        if (currentDepth > maxDepth) {
            return '[DepthLimit]';
        }
        if (obj && typeof obj.toJSON === 'function') {
            obj = obj.toJSON()
        }

        // var string = JSON.stringify(obj)

        // if (string === undefined) {
        //   return string
        // }

        // var length = maxLength - currentIndent.length - reserved

        if (Array.isArray(obj)) {
            //   var prettified = prettify(string)
            return JSON.stringify(obj);
        }
        if (typeof obj === 'object' && obj !== null) {
            var nextDepth = currentDepth + 1;
            var items = [];
            var delimiters;
            var comma = function (array, index) {
                return (index === array.length - 1 ? 0 : 1)
            }

            Object.keys(obj).forEach(function (key, index, array) {

                var keyPart = key + ': ';
                var val = obj[key];
                if (seen.indexOf(val) >= 0) {
                    //prevent cyclic
                    items.push(keyPart + "[Circular ~]");
                } else {
                    var value = _stringify(val, nextDepth,
                        keyPart.length + comma(array, index))
                    if (value !== undefined) {
                        items.push(keyPart + value)
                    }
                }

            })
            delimiters = '{}'

            if (items.length > 0) {
                return [
                    delimiters[0],
                    items.join(',\n' + createString(" ", nextDepth * indent)),
                    delimiters[1]
                ].join('\n' + createString(" ", nextDepth * indent))
            }
        }

        return JSON.stringify(obj);
    } (obj, 0, 0))
}

// Note: This regex matches even invalid JSON strings, but since we’re
// working on the output of `JSON.stringify` we know that only valid strings
// are present (unless the user supplied a weird `options.indent` but in
// that case we don’t care since the output would be invalid anyway).
var stringOrChar = /("(?:[^"]|\\.)*")|[:,]/g

function prettify(string) {
    return string.replace(stringOrChar, function (match, string) {
        return string ? match : match + ' '
    })
}

function get(options, name, defaultValue) {
    return (name in options ? options[name] : defaultValue)
}
namespace util {
    var formatRegExp = /%[sdj%]/g;
    exports.format = function () {

        let currentformat = undefined;

        var objects = [];
        let current;
        for (var i = 0; i < arguments.length; i++) {
            current = arguments[i];
            if (isString(current)) {
                if (current[0] == '%') {
                    currentformat = current;
                    continue;
                } else {
                    objects.push(current);
                }
            } else {
                if (currentformat) {
                    switch (currentformat) {
                        case '%s': objects.push(String(current));
                        case '%d': objects.push(Number(current));
                        case '%j':
                            try {
                                objects.push(stringify(current));
                            } catch (_) {
                                objects.push('[Circular]');
                            }
                        default:
                            objects.push(inspect(current));
                    }
                    currentformat = undefined;
                } else {
                    objects.push(inspect(current));
                }
            }
        }
        return objects.join(' ');
    };

    var debugs = {};
    var debugEnviron;
    exports.debuglog = function (set) {
        if (isUndefined(debugEnviron))
            debugEnviron = process.env.NODE_DEBUG || '';
        set = set.toUpperCase();
        if (!debugs[set]) {
            if (new RegExp('\\b' + set + '\\b', 'i').test(debugEnviron)) {
                var pid = process.pid;
                debugs[set] = function () {
                    var msg = exports.format.apply(exports, arguments);
                    console.error('%s %d: %s', set, pid, msg);
                };
            } else {
                debugs[set] = function () { };
            }
        }
        return debugs[set];
    };


    /**
     * Echos the value of a value. Trys to print the value out
     * in the best way possible given the different types.
     *
     * @param {Object} obj The object to print out.
     * @param {Object} opts Optional options object that alters the output.
     */
    /* legacy: obj, showHidden, depth, colors*/
    function inspect(obj, opts?) {
        // default options
        var ctx: any = {
            seen: [],
            stylize: stylizeNoColor
        };
        // legacy...
        if (arguments.length >= 3) ctx.depth = arguments[2];
        if (arguments.length >= 4) ctx.colors = arguments[3];
        if (isBoolean(opts)) {
            // legacy...
            ctx.showHidden = opts;
        } else if (opts) {
            // got an "options" object
            exports._extend(ctx, opts);
        }
        // set default options
        if (isUndefined(ctx.showHidden)) ctx.showHidden = false;
        if (isUndefined(ctx.depth)) ctx.depth = 2;
        if (isUndefined(ctx.colors)) ctx.colors = false;
        if (isUndefined(ctx.customInspect)) ctx.customInspect = true;
        if (ctx.colors) ctx.stylize = stylizeWithColor;
        return formatValue(ctx, obj, ctx.depth);
    }
    exports.inspect = inspect;


    // http://en.wikipedia.org/wiki/ANSI_escape_code#graphics
    const colors = {
        'bold': [1, 22],
        'italic': [3, 23],
        'underline': [4, 24],
        'inverse': [7, 27],
        'white': [37, 39],
        'grey': [90, 39],
        'black': [30, 39],
        'blue': [34, 39],
        'cyan': [36, 39],
        'green': [32, 39],
        'magenta': [35, 39],
        'red': [31, 39],
        'yellow': [33, 39]
    };

    // Don't use 'blue' not visible on cmd.exe
    const styles = {
        'special': 'cyan',
        'number': 'yellow',
        'boolean': 'yellow',
        'undefined': 'grey',
        'null': 'bold',
        'string': 'green',
        'date': 'magenta',
        // "name": intentionally not styling
        'regexp': 'red'
    };


    function stylizeWithColor(str, styleType) {
        var style = styles[styleType];

        if (style) {
            return '\u001b[' + colors[style][0] + 'm' + str +
                '\u001b[' + colors[style][1] + 'm';
        } else {
            return str;
        }
    }


    function stylizeNoColor(str, styleType) {
        return str;
    }


    function arrayToHash(array) {
        var hash = {};

        array.forEach(function (val, idx) {
            hash[val] = true;
        });

        return hash;
    }
    function stringify(value, ctx?) {
        if (isUndefined(value)) {
            if (ctx) return ctx.stylize('undefined', 'undefined');
            return 'undefined';
        }
        return prettyStringify(value, { indent: 4, maxLength: 2000 });
    }

    function formatValue(ctx, value, recurseTimes) {
        return stringify(value, ctx);
        // Provide a hook for user-specified inspect functions.
        // Check that value is an object with an inspect function on it
        if (ctx.customInspect &&
            value &&
            isFunction(value.inspect) &&
            // Filter out the util module, it's inspect function is special
            value.inspect !== exports.inspect &&
            // Also filter out any prototype objects using the circular check.
            !(value.constructor && value.constructor.prototype === value)) {
            var ret = value.inspect(recurseTimes, ctx);
            if (!isString(ret)) {
                ret = formatValue(ctx, ret, recurseTimes);
            }
            return ret;
        }

        // Primitive types cannot have properties
        var primitive = formatPrimitive(ctx, value);
        if (primitive) {
            return primitive;
        }

        // Look up the keys of the object.
        var keys = Object.keys(value);
        var visibleKeys = arrayToHash(keys);

        if (ctx.showHidden) {
            keys = Object.getOwnPropertyNames(value);
        }

        // IE doesn't make error fields non-enumerable
        // http://msdn.microsoft.com/en-us/library/ie/dww52sbt(v=vs.94).aspx
        if (isError(value)
            && (keys.indexOf('message') >= 0 || keys.indexOf('description') >= 0)) {
            return formatError(value);
        }

        // Some type of object without properties can be shortcutted.
        if (keys.length === 0) {
            if (isFunction(value)) {
                var name = value.name ? ': ' + value.name : '';
                return ctx.stylize('[Function' + name + ']', 'special');
            }
            if (isRegExp(value)) {
                return ctx.stylize(RegExp.prototype.toString.call(value), 'regexp');
            }
            if (isDate(value)) {
                return ctx.stylize(Date.prototype.toString.call(value), 'date');
            }
            if (isError(value)) {
                return formatError(value);
            }
        }

        var base = '', array = false, braces = ['{', '}'];

        // Make Array say that they are Array
        if (isArray(value)) {
            array = true;
            braces = ['[', ']'];
        }

        // Make functions say that they are functions
        if (isFunction(value)) {
            var n = value.name ? ': ' + value.name : '';
            base = ' [Function' + n + ']';
        }

        // Make RegExps say that they are RegExps
        if (isRegExp(value)) {
            base = ' ' + RegExp.prototype.toString.call(value);
        }

        // Make dates with properties first say the date
        if (isDate(value)) {
            base = ' ' + Date.prototype.toUTCString.call(value);
        }

        // Make error with message first say the error
        if (isError(value)) {
            base = ' ' + formatError(value);
        }

        if (keys.length === 0 && (!array || value.length == 0)) {
            return braces[0] + base + braces[1];
        }

        if (recurseTimes < 0) {
            if (isRegExp(value)) {
                return ctx.stylize(RegExp.prototype.toString.call(value), 'regexp');
            } else {
                return ctx.stylize('[Object]', 'special');
            }
        }

        ctx.seen.push(value);

        var output;
        if (array) {
            output = formatArray(ctx, value, recurseTimes, visibleKeys, keys);
        } else {
            return stringify(value, ctx);

        }

        ctx.seen.pop();

        return reduceToSingleString(output, base, braces);
    }


    function formatPrimitive(ctx, value) {
        if (isUndefined(value))
            return ctx.stylize('undefined', 'undefined');
        if (isString(value)) {
            var simple = '\'' + stringify(value).replace(/^"|"$/g, '')
                .replace(/'/g, "\\'")
                .replace(/\\"/g, '"') + '\'';
            return ctx.stylize(simple, 'string');
        }
        if (isNumber(value))
            return ctx.stylize('' + value, 'number');
        if (isBoolean(value))
            return ctx.stylize('' + value, 'boolean');
        // For some reason typeof null is "object", so special case here.
        if (isNull(value))
            return ctx.stylize('null', 'null');
    }


    function formatError(value) {
        return '[' + Error.prototype.toString.call(value) + ']';
    }


    function formatArray(ctx, value, recurseTimes, visibleKeys, keys) {
        var output = [];
        var realMax = value.length;
        var max = realMax;
        if (ctx.maxArrayLength >= 0) {
            max = Math.min(max, ctx.maxArrayLength);
        }
        for (var i = 0, l = max; i < l; ++i) {
            if (hasOwnProperty(value, String(i))) {
                output.push(formatProperty(ctx, value, recurseTimes, visibleKeys,
                    String(i), true));
            } else {
                output.push('');
            }
        }
        if (realMax > max) {
            output.push(' ...' + (realMax - max) + ' more');
        }
        keys.forEach(function (key) {
            if (!key.match(/^\d+$/)) {
                output.push(formatProperty(ctx, value, recurseTimes, visibleKeys,
                    key, true));
            }
        });
        return output;
    }


    function formatProperty(ctx, value, recurseTimes, visibleKeys, key, array) {
        var name, str, desc;
        desc = Object.getOwnPropertyDescriptor(value, key) || { value: value[key] };
        if (desc.value == undefined) {
            return;
        }
        if (desc.get) {
            if (desc.set) {
                str = ctx.stylize('[Getter/Setter]', 'special');
            } else {
                str = ctx.stylize('[Getter]', 'special');
            }
        } else {
            if (desc.set) {
                str = ctx.stylize('[Setter]', 'special');
            }
        }
        if (!hasOwnProperty(visibleKeys, key)) {
            name = '[' + key + ']';
        }
        if (!str) {
            if (ctx.seen.indexOf(desc.value) < 0) {
                if (isNull(recurseTimes)) {
                    str = formatValue(ctx, desc.value, null);
                } else {
                    str = formatValue(ctx, desc.value, recurseTimes - 1);
                }
                if (str.indexOf('\n') > -1) {
                    if (array) {
                        str = str.split('\n').map(function (line) {
                            return '  ' + line;
                        }).join('\n').substr(2);
                    } else {
                        str = '\n' + str.split('\n').map(function (line) {
                            return '   ' + line;
                        }).join('\n');
                    }
                }
            } else {
                str = ctx.stylize('[Circular]', 'special');
            }
        }
        if (isUndefined(name)) {
            if (array && key.match(/^\d+$/)) {
                return str;
            }
            name = stringify('' + key);
            if (name.match(/^"([a-zA-Z_][a-zA-Z_0-9]*)"$/)) {
                name = name.substr(1, name.length - 2);
                name = ctx.stylize(name, 'name');
            } else {
                name = name.replace(/'/g, "\\'")
                    .replace(/\\"/g, '"')
                    .replace(/(^"|"$)/g, "'");
                name = ctx.stylize(name, 'string');
            }
        }

        return name + ': ' + str;
    }


    function reduceToSingleString(output, base, braces) {
        var numLinesEst = 0;
        var length = output.reduce(function (prev, cur) {
            numLinesEst++;
            if (cur.indexOf('\n') >= 0) numLinesEst++;
            return prev + cur.replace(/\u001b\[\d\d?m/g, '').length + 1;
        }, 0);

        if (length > 60) {
            return braces[0] +
                (base === '' ? '' : base + '\n ') +
                ' ' +
                output.join(',\n  ') +
                ' ' +
                braces[1];
        }

        return braces[0] + base + ' ' + output.join(', ') + ' ' + braces[1];
    }


    // NOTE: These type checking functions intentionally don't use `instanceof`
    // because it is fragile and can be easily faked with `Object.create()`.
    function isArray(ar) {
        return Array.isArray(ar);
    }
    exports.isArray = isArray;

    function isBoolean(arg) {
        return typeof arg === 'boolean';
    }
    exports.isBoolean = isBoolean;

    function isNull(arg) {
        return arg === null;
    }
    exports.isNull = isNull;

    function isNullOrUndefined(arg) {
        return arg == null;
    }
    exports.isNullOrUndefined = isNullOrUndefined;

    function isNumber(arg) {
        return typeof arg === 'number';
    }
    exports.isNumber = isNumber;

    function isString(arg) {
        return typeof arg === 'string';
    }
    exports.isString = isString;

    function isSymbol(arg) {
        return typeof arg === 'symbol';
    }
    exports.isSymbol = isSymbol;

    function isUndefined(arg) {
        return arg === void 0;
    }
    exports.isUndefined = isUndefined;

    function isRegExp(re) {
        return isObject(re) && objectToString(re) === '[object RegExp]';
    }
    exports.isRegExp = isRegExp;

    function isObject(arg) {
        return typeof arg === 'object' && arg !== null;
    }
    exports.isObject = isObject;

    function isDate(d) {
        return isObject(d) && objectToString(d) === '[object Date]';
    }
    exports.isDate = isDate;

    function isError(e) {
        return isObject(e) &&
            (objectToString(e) === '[object Error]' || e instanceof Error);
    }
    exports.isError = isError;

    function isFunction(arg) {
        return typeof arg === 'function';
    }
    exports.isFunction = isFunction;

    function isPrimitive(arg) {
        return arg === null ||
            typeof arg === 'boolean' ||
            typeof arg === 'number' ||
            typeof arg === 'string' ||
            typeof arg === 'symbol' ||  // ES6 symbol
            typeof arg === 'undefined';
    }
    exports.isPrimitive = isPrimitive;



    exports.isBuffer = function isBuffer(arg) {
        return arg && typeof arg === 'object'
            && typeof arg.copy === 'function'
            && typeof arg.fill === 'function'
            && typeof arg.readUInt8 === 'function';
    };

    function objectToString(o) {
        return Object.prototype.toString.call(o);
    }


    function pad(n) {
        return n < 10 ? '0' + n.toString(10) : n.toString(10);
    }


    var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep',
        'Oct', 'Nov', 'Dec'];

    // 26 Feb 16:19:34
    function timestamp() {
        var d = new Date();
        var time = [pad(d.getHours()),
        pad(d.getMinutes()),
        pad(d.getSeconds())].join(':');
        return [d.getDate(), months[d.getMonth()], time].join(' ');
    }


    // log is just a thin wrapper to console.log that prepends a timestamp
    exports.log = function () {
        console.log('%s - %s', timestamp(), exports.format.apply(exports, arguments));
    };

    /**
 * Inherit the prototype methods from one constructor into another.
 *
 * The Function.prototype.inherits from lang.js rewritten as a standalone
 * function (not on Function.prototype). NOTE: If this file is to be loaded
 * during bootstrapping this function needs to be rewritten using some native
 * functions as prototype setup using normal JavaScript does not work as
 * expected during bootstrapping (see mirror.js in r114903).
 *
 * @param {function} ctor Constructor function which needs to inherit the
 *     prototype.
 * @param {function} superCtor Constructor function to inherit prototype from.
 */
    exports.inherits = require('inherits');


    exports._extend = function (origin, add) {
        // Don't do anything if add isn't an object
        if (!add || !isObject(add)) return origin;

        var keys = Object.keys(add);
        var i = keys.length;
        while (i--) {
            origin[keys[i]] = add[keys[i]];
        }
        return origin;
    };

    function hasOwnProperty(obj, prop) {
        return Object.prototype.hasOwnProperty.call(obj, prop);
    }

}