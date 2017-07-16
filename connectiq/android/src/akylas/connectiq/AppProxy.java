package akylas.connectiq;

import com.garmin.android.connectiq.ConnectIQ.IQApplicationEventListener;
import com.garmin.android.connectiq.ConnectIQ.IQApplicationInfoListener;
import com.garmin.android.connectiq.ConnectIQ.IQMessageStatus;
import com.garmin.android.connectiq.ConnectIQ.IQOpenApplicationListener;
import com.garmin.android.connectiq.ConnectIQ.IQOpenApplicationStatus;
import com.garmin.android.connectiq.ConnectIQ.IQSendMessageListener;
import com.garmin.android.connectiq.exception.InvalidStateException;


import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;

import com.garmin.android.connectiq.IQApp;
import com.garmin.android.connectiq.IQDevice;

@Kroll.proxy(creatableInModule = AkylasConnectiqModule.class)
public class AppProxy extends DeviceProxy
        implements IQApplicationEventListener {
    private String mAppIdentifier = null;
    private IQApp mApp;
    private boolean registeredForMessage = false;

    @Override
    public void handleCreationDict(HashMap dict, KrollProxy rootProxy) {
        super.handleCreationDict(dict, rootProxy);
        mAppIdentifier = TiConvert.toString(dict, "appId", null);
    }

    public IQApp getApp() {
        if (mApp == null) {
            if (mAppIdentifier != null) {
                mApp = new IQApp(mAppIdentifier);
            }
        }
        return mApp;
    }

    @Override
    public void eventListenerAdded(String type, int count,
            final KrollProxy proxy) {
        super.eventListenerAdded(type, count, proxy);
        if (type.equals(TiC.EVENT_PROPERTY_MESSAGE) && !registeredForMessage) {
            getApp();
            getDevice();
            if (mApp != null && mDevice != null) {
                try {
                    AkylasConnectiqModule.getConnectIQ()
                            .registerForAppEvents(getDevice(), getApp(), this);
                    registeredForMessage = true;
                } catch (InvalidStateException e) {
                    fireError(e);
                }
            }
        }
    }

    @Override
    public void eventListenerRemoved(String type, int count, KrollProxy proxy) {
        super.eventListenerRemoved(type, count, proxy);
        if (type.equals(TiC.EVENT_PROPERTY_MESSAGE) && count == 1
                && registeredForMessage) {
            registeredForMessage = false;
            if (mApp != null && mDevice != null) {
                try {
                    AkylasConnectiqModule.getConnectIQ()
                            .unregisterForApplicationEvents(mDevice, mApp);
                } catch (InvalidStateException e) {
                    fireError(e);
                }
            }
        }
    }

    @Override
    public void onMessageReceived(IQDevice device, IQApp app,
            List<Object> message, IQMessageStatus status) {

        KrollDict data = new KrollDict();
        data.put("data", message.toArray());
        data.put("status", status.ordinal());
        fireEvent(TiC.EVENT_PROPERTY_MESSAGE, data, true, false);
    }

    @Kroll.method
    public void sendMessage(Object data,
            final @Kroll.argument(optional = true) KrollFunction callback) {
        try {
            getConnectIQ().sendMessage(getDevice(), getApp(), data,
                    new IQSendMessageListener() {
                        @Override
                        public void onMessageStatus(IQDevice device, IQApp app,
                                IQMessageStatus status) {
                            if (callback != null) {
                                callback.callAsync(getKrollObject(),
                                        new Object[] { status });
                            }
                        }
                    });
        } catch (Exception e) {
            fireError(e);
            if (callback != null) {
//                KrollDict event = new KrollDict();
//                event.put(TiC.PROPERTY_ERROR, dictFromError(e));
                callback.callAsync(getKrollObject(), dictFromError(e));
            }
        }
    }

    @Kroll.method
    public void open(
            final @Kroll.argument(optional = true) KrollFunction callback) {
        try {
            IQDevice device = getDevice();
            if (device == null) {
                throw new Exception("cant access device");
            }
            getConnectIQ().openApplication(device, getApp(),
                    new IQOpenApplicationListener() {

                        @Override
                        public void onOpenApplicationResponse(IQDevice device,
                                IQApp app, IQOpenApplicationStatus status) {
                            if (callback != null) {
                                callback.callAsync(getKrollObject(),
                                        new Object[] { status });
                            }
                        }
                    });
        } catch (Exception e) {
            fireError(e);
            if (callback != null) {
//                KrollDict event = new KrollDict();
//                event.put(TiC.PROPERTY_ERROR, dictFromError(e));
                callback.callAsync(getKrollObject(), dictFromError(e));
            }
        }
    }

    @Kroll.method
    public void getInfo(
            final @Kroll.argument(optional = true) KrollFunction callback) {
        try {
            IQDevice device = getDevice();
            if (device == null) {
                throw new Exception("cant access device");
            }
            getConnectIQ().getApplicationInfo(mAppIdentifier, device,
                    new IQApplicationInfoListener() {
                        
                        @Override
                        public void onApplicationNotInstalled(String appId) {
                            KrollDict error = new KrollDict();
                            error.putCodeAndMessage(AkylasConnectiqModule.ERROR_APP_NOT_INSTALLED, "not installed");
                            callback.callAsync(getKrollObject(), error);
                        }
                        
                        @Override
                        public void onApplicationInfoReceived(IQApp app) {
                            if (callback != null) {
                                KrollDict event = new KrollDict();
                                event.put("status", app.getStatus());
                                event.put("id", app.getApplicationId());
                                event.put("name", app.getDisplayName());
                                callback.callAsync(getKrollObject(), event);
                            }
                            
                        }
                    });
        } catch (Exception e) {
            fireError(e);
            if (callback != null) {
//                KrollDict event = new KrollDict();
//                event.put(TiC.PROPERTY_ERROR, dictFromError(e));
                callback.callAsync(getKrollObject(), dictFromError(e));
            }
        }
    }
}
