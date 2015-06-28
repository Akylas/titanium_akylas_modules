/**
 * Akylas
 * Copyright (c) 2009-2010 by Akylas. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package akylas.bluetooth;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiBaseActivity;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.util.TiConvert;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

@Kroll.proxy(creatableInModule=AkylasBluetoothModule.class, propertyAccessors = {
	 TiC.PROPERTY_ADDRESS
})
public class BLEDeviceProxy extends KrollProxy
{
	private static final String TAG = "BluetoothDeviceProxy";
    private int mState = AkylasBluetoothModule.STATE_DISCONNECTED;
    private UartService mService = null;
  
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    
    private BluetoothDevice mDevice = null;
    private String mMacAdress = null;
        
    public BluetoothDevice getDevice() {
        if (mDevice == null) {
            if (mMacAdress != null) {
                mDevice = AkylasBluetoothModule.getBTAdapter().getRemoteDevice(mMacAdress);
            }
        }
        return mDevice;
    }
    
	public BLEDeviceProxy()
	{
		super();
	}

	public BLEDeviceProxy(TiContext tiContext)
	{
		this();
	}
	
	public void handleCreationDict(KrollDict dict)
    {
        super.handleCreationDict(dict);
        mMacAdress = TiConvert.toString(dict, "identifier", null);
    }

	@Override
	protected void initActivity(Activity activity) {
		super.initActivity(activity);
		((TiBaseActivity) activity).addOnLifecycleEventListener(this);
		IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        activity.registerReceiver(mReceiver, filter);
	}
	
//	@Override
//    public void onResume(Activity activity) {
//        super.onResume(activity);
//    }

//    @Override
//    public void onPause(Activity activity) {
//        super.onPause(activity);
//    }

    @Override
    public void onStop(Activity activity) {
        super.onStop(activity);
        stop();
    }
    
    private void stop() {
        getActivity().unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;
        setState(AkylasBluetoothModule.STATE_DISCONNECTED);
   }

    @Override
    public void onDestroy(Activity activity) {
        try {
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(mReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        } 
        stop();
        super.onDestroy(activity);
    }
	
	@Override
	public String getApiName()
	{
		return "Ti.Bluetooth.BLEDevice";
	}
	
	//UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
                mService = ((UartService.LocalBinder) rawBinder).getService();
                Log.d(TAG, "onServiceConnected mService= " + mService);
                if (!mService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    stop();
                } else {
                    mService.connect(mMacAdress);
                }

        }

        public void onServiceDisconnected(ComponentName classname) {
       ////     mService.disconnect(mDevice);
                setState(AkylasBluetoothModule.STATE_DISCONNECTED);
                mService = null;
        }
    };
    
    private synchronized void setState(int state) {
        if (state != mState) {
            mState = state;
            if (state == AkylasBluetoothModule.STATE_CONNECTED) {
                fireEvent("connected");
            } else if (state == AkylasBluetoothModule.STATE_DISCONNECTED) {
                fireEvent("disconnected");
            }
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

//            final Intent mIntent = intent;
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                setState(AkylasBluetoothModule.STATE_CONNECTED);
            }
           
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                setState(AkylasBluetoothModule.STATE_DISCONNECTED);
            }
          
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                if (mService != null) {
                    mService.enableTXNotification();
                }
            }
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
              
                 final byte[] bytes = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                 long timestamp = (new Date()).getTime();
                 int length = bytes.length;
                 if ( length >= 0 && hasListeners("read", false)) {
                     KrollDict data = new KrollDict();
                     data.put(TiC.PROPERTY_TIMESTAMP, timestamp);
                     data.put(TiC.PROPERTY_LENGTH,  length);
                     data.put(TiC.PROPERTY_DATA, TiBlob.blobFromObject(bytes));
                     fireEvent("read", data, false, false);
                 }
             }
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
                fireError(-1, "device does not support UART");
                mService.disconnect();
            }
        }
    };
    
    private void fireError(int code, String message) {
        if (hasListeners(TiC.EVENT_ERROR)) {
            KrollDict data = new KrollDict();
            data.putCodeAndMessage(code, message);
            fireEvent(TiC.EVENT_ERROR, data);
        }
    }

    private void startService() {
        if (mService != null) {
            return; //already done
        }
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        Intent bindIntent = new Intent(activity, UartService.class);
        activity.bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, makeGattUpdateIntentFilter());
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }
    
    public synchronized int getState() {
        return mState;
    }
	
	@Kroll.method @Kroll.getProperty
    public boolean getConnected() {
        return getState() == AkylasBluetoothModule.STATE_CONNECTED;
    }
	
	@Kroll.method @Kroll.getProperty
    public boolean getPaired() {
        return mDevice != null && mDevice.getBondState() == BluetoothDevice.BOND_BONDED;
    }
	
	@Kroll.method
    public void pair() {
	    if (!getPaired()) {
	        AkylasBluetoothModule.pairDevice(mDevice);
	    }
    }
	
	@Kroll.method
    public void connect() {
	    if (mMacAdress == null) {
	        fireError(-1, "no address supplied");
	        return;
	    }
	    startService();
    }
	
	@Kroll.method
    public void disconnect() {
	    stop();
    }
	
	@Kroll.method
    public void send(Object args)
	{
	    if (mService != null) {
	        if (args instanceof String) {
	            try {
	                mService.writeRXCharacteristic((TiConvert.toString(args)).getBytes("UTF-8"));
	            } catch (UnsupportedEncodingException e) {
	                e.printStackTrace();
	            }
	        } else if (args instanceof Object[]) {
	        } else if (args instanceof TiBlob) {
	            mService.writeRXCharacteristic(((TiBlob)args).getBytes());
	        }
	    }
	   
	}
}
