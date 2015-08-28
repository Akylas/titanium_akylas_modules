/**
 * Akylas
 * Copyright (c) 2009-2010 by Akylas. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package akylas.bluetooth;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.UUID;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiBaseActivity;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.proxy.TiEnhancedServiceProxy;
import org.appcelerator.titanium.util.TiConvert;

import akylas.bluetooth.BLEService.BLEServiceCallback;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;

@Kroll.proxy(creatableInModule=AkylasBluetoothModule.class, propertyAccessors = {
	 TiC.PROPERTY_ADDRESS
})
public class BLEDeviceProxy extends TiEnhancedServiceProxy implements BLEServiceCallback
{
	private static final String TAG = "BluetoothDeviceProxy";
    private int mState = AkylasBluetoothModule.STATE_DISCONNECTED;
    
    public static final UUID RX_SERVICE_UUID    = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID       = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID       = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    
    
    public static final UUID FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
  
    private boolean uartMode = true;
    
    BLEService tiService = null;
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
	
    @SuppressWarnings("rawtypes")
    @Override
    protected Class serviceClass() {
        return BLEService.class;
    }

    @Override
    public void unbindService() {
        super.unbindService();
        this.tiService.setBLEServiceCb(null);
        this.tiService = null;
    }

    protected void invokeBoundService() {
        this.tiService = (BLEService) this.service;
        this.tiService.setBLEServiceCb(this);
        this.tiService.connect(mMacAdress);
        super.invokeBoundService();
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
        stopService();
        setState(AkylasBluetoothModule.STATE_DISCONNECTED);
   }

    @Override
    public void onDestroy(Activity activity) {
        stop();
        super.onDestroy(activity);
    }
	
	@Override
	public String getApiName()
	{
		return "Ti.Bluetooth.BLEDevice";
	}
	
    private synchronized void setState(int state) {
        if (state != mState) {
            mState = state;
            if (state == AkylasBluetoothModule.STATE_CONNECTED) {
                AkylasBluetoothModule.getInstance().onDeviceConnected(this);
                fireEvent("connected");
            } else if (state == AkylasBluetoothModule.STATE_DISCONNECTED) {
                AkylasBluetoothModule.getInstance().onDeviceDisconnected(this);
                fireEvent("disconnected");
            }
        }
    }

 
    
    private void fireError(int code, String message) {
        if (hasListeners(TiC.EVENT_ERROR)) {
            KrollDict data = new KrollDict();
            data.putCodeAndMessage(code, message);
            fireEvent(TiC.EVENT_ERROR, data);
        }
    }

    public synchronized int getState() {
        return mState;
    }
	
	@Kroll.method @Kroll.getProperty
    public boolean getConnected() {
        return getState() == AkylasBluetoothModule.STATE_CONNECTED;
    }
	
//	@Kroll.method @Kroll.getProperty
//    public boolean getPaired() {
//        return mDevice != null && mDevice.getBondState() == BluetoothDevice.BOND_BONDED;
//    }
//	
//	@Kroll.method
//    public void pair() {
//	    if (!getPaired()) {
//	        AkylasBluetoothModule.pairDevice(mDevice);
//	    }
//    }
	
	@Kroll.method
    public void connect() {
	    if (mMacAdress == null) {
	        fireError(-1, "no address supplied");
	        return;
	    }
	    if (this.tiService == null) {
	        startService();
	    } else {
	        this.tiService.connect(mMacAdress);
	    }
    }
	
	
	private static UUID getUUIDFromString(final String uuid) {
	    if (uuid.length() == 4) {
	        return getUUIDFromString("0000" + uuid + "-0000-1000-8000-00805f9b34fb");
	    } else {
	        return UUID.fromString(uuid);
	    }
	}
	
	private static String stringFromUUID(final UUID uuid) {
	    String result = uuid.toString();
	    if (result.endsWith("-0000-1000-8000-00805f9b34fb")) {
	        result = result.substring(4, 8);
	    }
	    return result;
	}
	
	@Kroll.method
    public void disconnect() {
	    stop();
    }
	
	@Kroll.method
    public void send(Object args)
	{
	    if (this.tiService != null && uartMode) {
	        byte[] bytes = null;
	        if (args instanceof String) {
	            try {
	                bytes = TiConvert.toString(args).getBytes("UTF-8");
	            } catch (UnsupportedEncodingException e) {
	                e.printStackTrace();
	            }
	        } else if (args instanceof Object[]) {
	        } else if (args instanceof TiBlob) {
	            bytes = ((TiBlob)args).getBytes();
	        }
	        if (bytes != null) {
	            BluetoothGattCharacteristic charac = this.tiService.getCharacteristic(RX_SERVICE_UUID, TX_CHAR_UUID);
	            tiService.writeCharacteristic(charac, bytes);
	        }
	    }
	   
	}
	
	@Kroll.method
    public void startCharacteristicNotifications(final String serviceUUID, final String charUUID)
    {
        if (this.tiService != null) {
            BluetoothGattCharacteristic charac = this.tiService.getCharacteristic(getUUIDFromString(serviceUUID), getUUIDFromString(charUUID));
            this.tiService.setCharacteristicNotification(charac, true);
        }
    }
	
	@Kroll.method
    public void stopCharacteristicNotifications(final String serviceUUID, final String charUUID)
    {
        if (this.tiService != null) {
            BluetoothGattCharacteristic charac = this.tiService.getCharacteristic(getUUIDFromString(serviceUUID), getUUIDFromString(charUUID));
            this.tiService.setCharacteristicNotification(charac, false);
        }
    }


	
	@Kroll.method
    public void readCharacteristicValue(final String serviceUUID, final String charUUID)
    {
	    if (this.tiService != null) {
            BluetoothGattCharacteristic charac = this.tiService.getCharacteristic(getUUIDFromString(serviceUUID), getUUIDFromString(charUUID));
	        this.tiService.readCharacteristic(charac);
	    }
    }

    @Override
    public void displayRssi(int rssi) {
        if (hasListeners(TiC.EVENT_CHANGE, false)) {
            KrollDict data = new KrollDict();
            data.put("rssi", rssi);
            fireEvent(TiC.EVENT_CHANGE, data, false, false);
        }
    }

    @Override
    public void onData(final BluetoothGattCharacteristic characteristic, byte[] bytes) {
        // TODO Auto-generated method stub
        long timestamp = (new Date()).getTime();
        int length = bytes.length;
        final String eventType = "read";
        if ( length >= 0 && hasListeners(eventType, false)) {
            KrollDict data = new KrollDict();
            data.put(TiC.PROPERTY_TIMESTAMP, timestamp);
            data.put(TiC.PROPERTY_LENGTH,  length);
            data.put("service",  stringFromUUID(characteristic.getService().getUuid()));
            data.put("characteristic",  stringFromUUID(characteristic.getUuid()));
            data.put(TiC.PROPERTY_DATA, TiBlob.blobFromObject(bytes));
            fireEvent(eventType, data, false, false);
        }
    }

    @Override
    public void notifyConnectedGATT() {
        if (!uartMode) {
            setState(AkylasBluetoothModule.STATE_CONNECTED);
        }
    }

    @Override
    public void notifyDisconnectedGATT() {
        setState(AkylasBluetoothModule.STATE_DISCONNECTED);
    }

    @Override
    public void onServicesDiscovered() {
        if (uartMode) {
            BluetoothGattCharacteristic charac = this.tiService.getCharacteristic(RX_SERVICE_UUID, RX_CHAR_UUID);
            this.tiService.setCharacteristicNotification(charac, true);
//            charac = this.tiService.getCharacteristic(DIS_UUID, FIRMWARE_REVISON_UUID);
//            this.tiService.readCharacteristic(charac);
            setState(AkylasBluetoothModule.STATE_CONNECTED);
        }
    }

    @Override
    public void nameChanged(String name) {
        setProperty(TiC.PROPERTY_NAME, name);
        if (hasListeners(TiC.EVENT_CHANGE, false)) {
            KrollDict data = new KrollDict();
            data.put(TiC.PROPERTY_NAME, name);
            fireEvent(TiC.EVENT_CHANGE, data, false, false);
        }
    }
}
