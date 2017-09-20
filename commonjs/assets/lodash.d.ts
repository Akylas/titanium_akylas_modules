declare module "mod" {
    module "lodash" {
        interface LoDashStatic {
            remove<T, K extends keyof T>(
                object: T,
                key?: K,
                defaultValue?: T[K]
            ): T[K]
            getKeyByValue(object, value)
            mapIfDefined(array, func)
            mod(n, m)
            move(array, oldIndex, newIndex)
            pick<T, K extends keyof T>(obj: T, ...keys: K[]): Pick<T, K>
        }
    }
}
declare var _: _.LoDashStatic;
