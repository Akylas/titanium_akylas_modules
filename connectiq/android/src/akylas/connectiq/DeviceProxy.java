package akylas.connectiq;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.ParentingProxy;
import org.appcelerator.titanium.util.TiConvert;

import com.garmin.android.connectiq.ConnectIQ;
import com.garmin.android.connectiq.ConnectIQ.IQDeviceEventListener;
import com.garmin.android.connectiq.IQDevice;
import com.garmin.android.connectiq.IQDevice.IQDeviceStatus;
import com.garmin.android.connectiq.exception.InvalidStateException;
import com.garmin.android.connectiq.exception.ServiceUnavailableException;

@Kroll.proxy(parentModule = AkylasConnectiqModule.class, creatableInModule = AkylasConnectiqModule.class)
public class DeviceProxy extends ParentingProxy
        implements IQDeviceEventListener {
    private Long mIdentifier = null;
    protected IQDevice mDevice = null;
    private boolean registeredForStatus = false;

    @Override
    public void handleCreationDict(HashMap dict, KrollProxy rootProxy) {
        setParentForBubbling(AkylasConnectiqModule.getInstance());
        super.handleCreationDict(dict, rootProxy);
        mIdentifier = TiConvert.toLong(dict, "id", -1);
    }

    public IQDevice getDevice() {
        if (mDevice == null) {
            if (mIdentifier != null) {
                mDevice = AkylasConnectiqModule.getInstance().getDevice(mIdentifier);
            }
        }
        return mDevice;
    }
    
    protected KrollDict dictFromError(Exception e) {
        int code = -1;
        if (e instanceof InvalidStateException) {
            code = AkylasConnectiqModule.ERROR_INVALID_STATE;
        } else if (e instanceof ServiceUnavailableException) {
            code = AkylasConnectiqModule.ERROR_SERVICE_UNAVAILABLE;
        }
        KrollDict data = new KrollDict();
        data.putCodeAndMessage(code, e.getMessage());
        return data;
    }
    
    protected boolean fireError(Exception e) {
        return fireEvent(TiC.EVENT_ERROR, dictFromError(e), true, false);
    }
    
    protected ConnectIQ getConnectIQ() {
        return AkylasConnectiqModule.getConnectIQ();
    }

    @Override
    public void eventListenerAdded(String type, int count,
            final KrollProxy proxy) {
        super.eventListenerAdded(type, count, proxy);
        if (type == "status" && !registeredForStatus) {
            registeredForStatus = true;
            getDevice();
            if (mDevice != null) {
                try {
                    getConnectIQ().registerForDeviceEvents(mDevice, this);
                } catch (InvalidStateException e) {
                    fireError(e);
                }
            }
        }
    }


    @Override
    public void eventListenerRemoved(String type, int count, KrollProxy proxy) {
        super.eventListenerRemoved(type, count, proxy);
        if (type == "status" && count == 1 && registeredForStatus) {
            registeredForStatus = false;
            if (mDevice != null) {
                try {
                    getConnectIQ().unregisterForDeviceEvents(mDevice);
                } catch (InvalidStateException e) {
                    fireError(e);
                }
            }
        }
    }

    @Override
    public void onDeviceStatusChanged(IQDevice arg0, IQDeviceStatus status) {
        KrollDict data = new KrollDict();
        data.put("status", status);
        fireEvent("status", data);
    }
}
