/// <reference path="/Volumes/data/mguillon/Library/Application Support/Titanium/mobilesdk/osx/6.2.0.AKYLAS/titanium.d.ts" />

declare function akPath(name: string, dir: string): string;
declare function akRequire(moduleId: string): any;
declare function akInclude(moduleId: string);
declare function debounce(func: (...args: any[]) => any, wait: number, immediate?: boolean): (...args: any[]) => any;
declare function sdebug(...strings: any[]);
declare function sinfo(...strings: any[]);
declare function serror(...strings: any[]);
declare function stringify(value: any, space?: string | number);
declare function tr(id: string, _default?: string): string;
declare function trc(id: string, _default?: string): string;
declare function tru(id: string, _default?: string): string;
declare function trt(id: string, _default?: string): string;
declare var __APPLE__: boolean;
declare var __ANDROID__: boolean;
declare var __PRODUCTION__: boolean;
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
        move(array, oldIndex, newIndex)
    }
}

declare var ak: AK.AK;

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

declare class View extends Ti.UI.View { GC?() }
declare class TiWindow extends Ti.UI.Window { winGCId?: string; GC?() }
declare class ButtonBar extends Ti.UI.ButtonBar { GC?() }
declare class TextArea extends Ti.UI.TextArea { GC?() }
declare class ImageView extends Ti.UI.ImageView { GC?() }
declare class OptionDialog extends Ti.UI.OptionDialog { GC?() }
declare class ListView extends Ti.UI.ListView { GC?() }
declare class CollectionView extends Ti.UI.ListView { GC?() }//temp needs doc on ti side for collection
declare class ListSection extends Ti.UI.ListSection { GC?() }
declare class CollectionSection extends Ti.UI.ListSection { GC?() } //temp needs doc on ti side for collection
declare class Button extends Ti.UI.Button { GC?() }
declare class ScrollableView extends Ti.UI.ScrollableView { GC?() }
declare class ScrollView extends Ti.UI.ScrollView { GC?() }
declare class TextField extends Ti.UI.TextField { GC?() }
declare class Label extends Ti.UI.Label { GC?() }
declare class AlertDialog extends Ti.UI.AlertDialog { GC?() }
declare class WebView extends Ti.UI.WebView { GC?() }
declare class HTTPClient extends Ti.Network.HTTPClient { GC?() }
declare class Window extends TiWindow { GC?() }
declare class TCPSocket extends Ti.Network.Socket.TCP { GC?() }

declare module AK {

    export interface IReduxFn {
        addNaturalConstructor(context: any, constructors: {}, arg1: string, arg2: string)
        style(type: any, obj?: any): any
        includeOverloadRJSS(...args: string[])
        includeRJSS(...args: string[])
        setDefault(selector:string, defaults, orientation?)
    }
    export interface IRedux {
        fn: IReduxFn
    }

    export class TiEmitter extends Emitter {
        addEventListener(name: string, callback: (...args: any[]) => any): this;
        removeEventListener(name: string, callback: (...args: any[]) => any): this;
        fireEvent(name: string, ...args): void;
    }

    export class Emitter {
        on(name: string, callback: (...args: any[]) => any): this;
        once(name: string, callback: (...args: any[]) => any): this;
        off(name: string, callback: (...args: any[]) => any): this;
        removeAllListeners(): this;
        emit(name: string, ...args);
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

}
declare module 'akylas.commonjs/AkInclude/App' {
    export class AkApp extends AK.App { }
}

declare interface ListEvent<T> {
    item: T,
    section: ListSection,
    sectionIndex: number,
    itemIndex: number,
    editing?: boolean
    accessoryClicked?: boolean
    searchResult?: boolean
    listView: ListView,
    bindId?: string
}
