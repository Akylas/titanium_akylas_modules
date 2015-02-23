package akylas.map;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class SoftCache {
    private HashMap<String, ArrayList<SoftReference<Object>>> multiMap;

    public SoftCache() {
        multiMap = new HashMap<String, ArrayList<SoftReference<Object>>>();
    }
    
    /* implement this to create new object of type T if cache is empty */
    public abstract Object runWhenCacheEmpty(String key);

    public void clear() {
        multiMap.clear();
    }
    /*
     * retrieves cached item or return null if cache is empty
     */
    public Object get(String key) {
        Object itemCached = null;
        if (!multiMap.containsKey(key) || multiMap.get(key).size() == 0) {
            return runWhenCacheEmpty(key);
        } else {
            /*
             * get the referent object and check if its already been GC if not
             * we re-use
             */
            SoftReference<Object> softRef = multiMap.get(key).get(0);
            Object obj = softRef.get();
            /*
             * if referent object is empty(due to GC) then caller must create a
             * new object
             */
            if (null == obj) {
                return runWhenCacheEmpty(key);
            }
            /*
             * otherwise restore from cache
             */
            else {
                itemCached = (softRef.get());
                multiMap.get(key).remove(softRef);
            }
        }
        return itemCached;
    }

    /*
     * saves a view object to the cache by reference, we use a multiMap to allow
     * duplicate IDs
     */
    public void put(String key, Object item) {
        SoftReference<Object> ref = new SoftReference<Object>(item);
        /*
         * check if we already have a reuseable layouts saved if so just add to
         * the list of reusable layouts
         */
        if (multiMap.containsKey(key)) {
            multiMap.get(key).add(ref);
        } else {
            /*
             * otherwise we have no reusable layouts lets create a list of
             * reusable layouts and add it to the multiMap
             */
            ArrayList<SoftReference<Object>> list = new ArrayList<SoftReference<Object>>();
            list.add(ref);
            multiMap.put(key, list);
        }
    }
}