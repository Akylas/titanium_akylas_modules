package akylas.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.KrollProxyListener;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiViewEventOverrideDelegate;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.KrollProxyReusableListener;
import org.appcelerator.titanium.view.TiTouchDelegate;
import org.appcelerator.titanium.view.TiUIView;

import android.app.Activity;

@Kroll.proxy
public class TiReusableProxy extends TiViewProxy implements KrollProxy.SetPropertyChangeListener, TiViewEventOverrideDelegate
{
    private static final String TAG = "TiReusableProxy";
    public class ReusableProxyItem {
        KrollProxy proxy;
        KrollDict initialProperties;
        KrollDict currentProperties;
        KrollDict diffProperties;
        
        public ReusableProxyItem(KrollProxy proxy, KrollDict props) {
            initialProperties = (KrollDict)props.clone();
            this.proxy = proxy;
            diffProperties = new KrollDict();
            currentProperties = new KrollDict();
        }
        
        public KrollProxy getProxy() {
            return proxy;
        }
        
        /**
         * This method compares applied properties of a view and our data model to
         * generate a new set of properties we need to set. It is crucial for scrolling performance. 
         * @param properties The properties from our data model
         * @return The difference set of properties to set
         */
        public KrollDict generateDiffProperties(KrollDict properties) {
            diffProperties.clear();
            Iterator<String> it = currentProperties.keySet().iterator();
            while (it.hasNext())
            {
                String appliedProp = it.next();
                if (properties == null || !properties.containsKey(appliedProp)) {
                    applyProperty(appliedProp, initialProperties.get(appliedProp), it);
                }
            }
            if (properties != null) { 
                it = properties.keySet().iterator();
                while (it.hasNext())
                {
                    String property = it.next();
                    Object value = properties.get(property);
                    Object existingVal = currentProperties.get(property);           
                    if (existingVal != value && (existingVal == null || value == null || !existingVal.equals(value))) {
                        applyProperty(property, value, it);
                    }
                }
            }
            return diffProperties;
            
        }
        
        private void applyProperty(String key, Object value, Iterator<String> it) {
            diffProperties.put(key, value);
            if (value == null)
                it.remove();
            else
                currentProperties.put(key, value);
        }
        
        public void setCurrentProperty(String key, Object value) {
            currentProperties.put(key, value);
        }

        public boolean containsKey(String key) {
            return initialProperties.containsKey(key);
        }
    }
    private ReusableProxyItem mItem;
    private HashMap<String, ReusableProxyItem> bindingsMap;
    private List<KrollProxy> nonBindingProxies;
    private String mReusableIdentifier;
    
    public TiReusableProxy() {    
        bindingsMap = new HashMap<String, ReusableProxyItem>();
        nonBindingProxies = new ArrayList();
    }
    @Override
    public TiUIView createView(Activity arg0) {
        return null;
    }
    
    public ReusableProxyItem getItem() {
        if (mItem == null) {
            mItem = new ReusableProxyItem(this, getProperties());
        }
        return mItem;
    }
    
    public void release()
    {
        super.release();
        bindingsMap.clear();
        nonBindingProxies.clear();
    }
    
    public KrollProxy getProxyFromBinding(String binding) {
        ReusableProxyItem viewItem = bindingsMap.get(binding);
        if (viewItem != null) {
            return viewItem.getProxy();
        }
        return null;
    }
    
    @Override
    public void addBinding(String bindId, KrollProxy arg)
    {
        super.addBinding(bindId, arg);
        KrollProxy bindingProxy = null;
        if (arg instanceof KrollProxy)
            bindingProxy = (KrollProxy) arg;
        if (bindingProxy == null) {
            return;
        }
        if (bindId != null) {
            ReusableProxyItem viewItem = new ReusableProxyItem(bindingProxy, bindingProxy.getProperties());
            bindingsMap.put(bindId, viewItem);
        }
        else {
            nonBindingProxies.add(bindingProxy);
        }
        
    }
    
    public HashMap<String, ReusableProxyItem> getBindings() {
        return bindingsMap;
    }
    
    public List<KrollProxy> getNonBindedProxies() {
        return nonBindingProxies;
    }

    @Override
    public void onSetProperty(KrollProxy proxy, String name, Object value) {
        for (Map.Entry<String, ReusableProxyItem> entry : bindingsMap.entrySet()) {
            String key = entry.getKey();
            ReusableProxyItem item = entry.getValue();
            if (item.getProxy() == proxy) {
                item.setCurrentProperty(name, value);
                return;
            }
        }
    }
    
    
    public void populateViews(KrollDict data, ReusableView cellContent, TiViewTemplate template, KrollDict extraData, boolean reusing) {
        // Handling root item, since that is not in the views map.
        if (cellContent == null) {
            return;
        }
                
        data = template.prepareDataDict(data);

        KrollDict listItemProperties;
        String itemId = null;

        if (data.containsKey(TiC.PROPERTY_PROPERTIES)) {
            listItemProperties = new KrollDict(
                    (HashMap) data.get(TiC.PROPERTY_PROPERTIES));
        } else {
            listItemProperties = new KrollDict();
        }
        ReusableProxyItem rootItem = getItem();

        // find out if we need to update itemId
        if (listItemProperties.containsKey(TiC.PROPERTY_ITEM_ID)) {
            itemId = TiConvert.toString(listItemProperties
                    .get(TiC.PROPERTY_ITEM_ID));
        }

        // update extra event data for list item
//        appendExtraEventData(cellContent,  TiC.PROPERTY_PROPERTIES, itemId, extraData);
        this.setEventOverrideDelegate(this);

        HashMap<String, ReusableProxyItem> views = getBindings();
        // Loop through all our views and apply default properties
        for (String binding : views.keySet()) {
            TiViewTemplate.DataItem dataItem = template.getDataItem(binding);
            ReusableProxyItem viewItem = views.get(binding);
            KrollProxy proxy  = viewItem.getProxy();
            KrollProxyListener modelListener = (KrollProxyListener) proxy.getModelListener();
            if (modelListener == null || !(modelListener instanceof KrollProxyReusableListener))
                continue;
            if (modelListener instanceof TiUIView && cellContent instanceof TiTouchDelegate) {
                ((TiUIView)modelListener).setTouchDelegate((TiTouchDelegate) cellContent);
            }
            proxy.setSetPropertyListener(this);
            proxy.setEventOverrideDelegate(this);
            // if binding is contain in data given to us, process that data,
            // otherwise
            // apply default properties.
            if (reusing) {
                ((KrollProxyReusableListener) modelListener).setReusing(true);
            }
            if (data.containsKey(binding) && modelListener != null) {
                HashMap map = (HashMap) data.get(binding);
                if (map != null) {
                    KrollDict properties = new KrollDict(map);
                    KrollDict diffProperties = viewItem
                            .generateDiffProperties(properties);
                    if (!diffProperties.isEmpty()) {
                        modelListener.processProperties(diffProperties);
                    }
                }
            } else if (dataItem != null && modelListener != null) {
                KrollDict diffProperties = viewItem
                        .generateDiffProperties(null);
                if (!diffProperties.isEmpty()) {
                    modelListener.processProperties(diffProperties);
                }
            } else {
                Log.w(TAG, "Sorry, " + binding
                        + " isn't a valid binding. Perhaps you made a typo?",
                        Log.DEBUG_MODE);
            }
            if (reusing) {
                ((KrollProxyReusableListener) modelListener).setReusing(false);
            }
        }
        
        for (KrollProxy theProxy : getNonBindedProxies()) {
            KrollProxyListener modelListener = (KrollProxyListener) theProxy.getModelListener();
            if (modelListener instanceof KrollProxyReusableListener) {
                if (modelListener instanceof TiUIView && cellContent instanceof TiTouchDelegate) {
                    ((TiUIView)modelListener).setTouchDelegate((TiTouchDelegate) cellContent);
                }
                theProxy.setEventOverrideDelegate(this);
            }
        }

        // process listItem properties
        KrollDict listItemDiff = getItem().generateDiffProperties(listItemProperties);
        if (!listItemDiff.isEmpty()) {
            cellContent.processProperties(listItemDiff);
        }
    }
    
    public String getReusableIdentifier() {
        return mReusableIdentifier;
    }
    
    public void generateContent(KrollDict data, TiViewTemplate template, final KrollDict extraData) {
        mReusableIdentifier = template.getTemplateID();
        ReusableView view = new ReusableView(this);
        
        setView(view);
        realizeViews();

        if (data != null && template != null) {
            populateViews(data, view, template, extraData, false);
        }
    }

    @Override
    public Object overrideEvent(Object data, String type, KrollProxy proxy) {
        if (data != null && !(data instanceof KrollDict)) {
            return data;
        }
        KrollDict dict = (KrollDict) data;
        if (dict == null) {
            dict = new KrollDict();
        }
        else if (dict.containsKey(TiC.PROPERTY_SECTION)) {
            return data; //already done
        }

        String bindId = TiConvert.toString(
                proxy.getProperty(TiC.PROPERTY_BIND_ID), null);
        if (bindId != null) {
            dict.put(TiC.PROPERTY_BIND_ID, bindId);
        }
        String itemId = TiConvert.toString(
                proxy.getProperty(TiC.PROPERTY_ITEM_ID), null);
        if (itemId != null) {
            dict.put(TiC.PROPERTY_ITEM_ID, itemId);
        }
        return dict;
    }
}
