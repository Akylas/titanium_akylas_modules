/// <reference path="/Volumes/data/mguillon/Library/Application Support/Titanium/mobilesdk/osx/6.1.0.AKYLAS/titanium.d.ts" />

declare function akPath(name: string, dir: string): string;
declare function akRequire(moduleId: string): any;
declare function akInclude(moduleId: string);
declare function debounce(func: Function, wait: number, immediate?: Boolean): Function;
declare function sdebug(...strings: any[]);
declare function sinfo(...strings: any[]);
declare function serror(...strings: any[]);
declare function stringify(value: any, space?: string | number);
declare function tr(id: string, _default?: string): string;
declare function trc(id: string, _default?: string): string;
declare function trt(id: string, _default?: string): string;
declare var __APPLE__: Boolean;
declare var __ANDROID__: Boolean;
declare var __PRODUCTION__: Boolean;
declare var __dirname: string;

declare module _ {
    interface LoDashStatic {
        remove<W>(
            object: any,
            key?: string,
            defaultValue?: W
        ): any
        getKeyByValue(object, value)
        mapIfDefined(array, func)
        mod(n, m)
        moveTo(array, oldIndex, newIndex)
    }
}



declare var ak: AK.IAK;
// declare var app: AKApp;

interface String {
    assign(...args)
}
interface Object {
    assignDeep(target: Object, ...sources): Object;
    defaults(target: Object, ...sources): Object;
    deepCopy(source: Object): Object;
    isObject(source): Boolean;
    get(source: Object, keys: string[]): any;
}

declare type Dict = Dictionary<any>;

declare class View extends Ti.UI.View implements AK.View {
    GC?()
}
// declare type Window = TiWindow;
declare class TiWindow extends Ti.UI.Window implements AK.View {
    GC?()
}


declare class BaseWindow extends TiWindow {
    // navWindow: Boolean
    isOpened: Boolean
    navWindow?: boolean
    manager?: NavWindow
    underContainer?: View
    openMe(args?)
    closeMe(args?)
    onOpen?(args?)
    onClose?(args?)
    toDoAfterOpening()
    shouldShowBackButton(backTitle: string)
    showLoading(args?)
    hideLoading(args?)
    GC()
    addPropertiesToGC(key: string)
}

declare class NavWindow extends AppWindow {
    window: AppWindow
    navOpenWindow(_win, _args?)
    createManagedWindow(constructor, args?)
    createAndOpenWindow(_constructor, _args?, _winArgs?)
    openWindow(_win, _args?, _dontCheckOpening?: Boolean)
    closeToRootWindow()
    canGCWindow(_win)
    isOpened: Boolean
}

declare class NavigationBar extends View {
    actualNavbar:View
    titleHolder:View
    leftButtonViewHolder:View
    rightButtonViewHolder:View
    setRootWindow(_window)
    setMyVisibility(_visible, _animated, _duration?)
    onstackchange?(event)
    onBackButton?(event)
}

declare class ButtonBar extends Ti.UI.ButtonBar { }
declare class TextArea extends Ti.UI.TextArea { }
declare class ImageView extends Ti.UI.ImageView { }
declare class OptionDialog extends Ti.UI.OptionDialog { }
declare class ListView extends Ti.UI.ListView { }
declare class CollectionView extends Ti.UI.ListView { }
declare class ListSection extends Ti.UI.ListSection { }
declare class Button extends Ti.UI.Button { }
declare class ScrollableView extends Ti.UI.ScrollableView { }
declare class ScrollView extends Ti.UI.ScrollView { }
declare class TextField extends Ti.UI.TextField { }
declare class Label extends Ti.UI.Label { }
declare class AlertDialog extends Ti.UI.AlertDialog { }
declare class HTTPClient extends Ti.Network.HTTPClient { }
declare class Window extends TiWindow { }



declare module AK {
    export interface View {
        GC?()
    }

    export interface IReduxFn {
        addNaturalConstructor(context: any, constructors: {}, arg1: string, arg2: string)
        style(type: any, obj?: any): any
        includeOverloadRJSS(...args: string[])
        includeRJSS(...args: string[])
    }
    export interface IRedux {
        fn: IReduxFn
    }

    export class TiEmitter extends Emitter {
        addEventListener(name: string, callback: (...args: any[]) => any): this;
        removeEventListener(name: string, callback: (...args: any[]) => any): this;
        fireEvent(name: string, ...args: any[]): void;
    }

    export class Emitter {
        on(name: string, callback: (...args: any[]) => any): this;
        off(name: string, callback: (...args: any[]) => any): this;
        emit(name: string, ...args: any[]): void;
        removeAllListeners(event?: string): this;
    }


    export interface IAK {
        ti: IAKTi
        locale: IAKLang
        getLocaleInfo()
        getAppInfo()
        prepareAppObject(_app: {})
    }
    export interface IAKTi {
        redux: IRedux
        constructors: any
        loadRjss(...string)
        loadCreators(_toLoad: string[], _endsWithJS?: boolean)
        loadCreatorsFromDir(dir: string)
        loadOverloadRjssFromDir(dir: string)
        loadRjssFromDir(dir: string)
        createFromConstructor(_constructor: string, _args?)
        create(_type: string, _args: {}, _defaults: {})
        style(_template: {}, _type?: string, _defaults?: {})
        add(view: Ti.UI.View, children: Ti.UI.View | {} | Array<Ti.UI.View | {}>, index?: number): void
    }
    export interface IAKLang {
        defaultLanguage: string
        currentLanguage: string
        storeMissingTranslations?:boolean
        appendLanguage(_context, _data: Object | string)
        loadLanguage(_context, _data?: Object)

    }
    export interface IWindowManager {
        // mainwindow: MainWindow
        androidNav?: boolean
        // slidemenu: SlideMenu
        // topWindow: TiWindow
        rootWindow: TiWindow
        createAndOpenWindow(_constructor: string, _args?, _openingArgs?, _dontCheckOpening?: Boolean)
        openWindow(_win, _args?, _dontCheckOpening?: Boolean)
        closeWindow(_win, _args?, _callGC?: Boolean)
        windowSignalsOpened(_win)
        getWindowManager(_win)
    }

    export class App extends TiEmitter {
        constructor(context, _args?)
        info: any
        deviceinfo: any
        modules: any
        values: any
        utilities: IUtils
        templates: any
        ui: IWindowManager
        main()
        closeApp()
        showAlert(args)
        debounce(callback: Function, time?: Number)
        onDebounce(object, type: string, callback: Function)
        composeFunc(...funcs: Function[])
        confirmAction(_args, _callback?: Function, _callbackCancel?: Function)
    }
}
declare module 'akylas.commonjs/AkInclude/App' {
    export class AkApp extends AK.App { }
}


declare class AppTabView extends View {
    pager: ScrollableView
    container: View
    currentView: View
    setTab(index: number)
    setTabs(_tabs)
    getTab(_index)
    getTabs()
    moveNext()
}

declare class AppTabController extends View {
    container: View
    setLabels(tabs: string[])
    addTab(tab: string)
    setIndex(index: number)
}

declare class TemplateModule {
    prepareTemplate(_template, _type?: string, _defaults?: Object)
    internalAddEvents (_template, _properties?) 
    internalAddProperties (_template, _properties?) 
    addProperties  (_template, _properties?)
    cloneTemplate (_template: string, _events?)
    cloneTemplateAndFill(_template, _properties?, _events?)
    getTemplate(_key: string)
    addTemplate(_template, _key: string)
}
