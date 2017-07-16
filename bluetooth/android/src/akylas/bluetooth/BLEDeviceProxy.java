/**
 * Akylas
 * Copyright (c) 2009-2010 by Akylas. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package akylas.bluetooth;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiBaseActivity;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiEnhancedServiceProxy;
import org.appcelerator.titanium.util.TiConvert;

import akylas.bluetooth.BLEService.BLEServiceCallback;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.support.v4.util.Pair;

@Kroll.proxy(creatableInModule = AkylasBluetoothModule.class, propertyAccessors = { TiC.PROPERTY_ADDRESS })
public class BLEDeviceProxy extends TiEnhancedServiceProxy implements
        BLEServiceCallback {
    private static final String TAG = "BluetoothDeviceProxy";
    private int mState = AkylasBluetoothModule.STATE_DISCONNECTED;

    public static final UUID RX_SERVICE_UUID = UUID
            .fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID
            .fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID
            .fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    public static final UUID FIRMWARE_REVISON_UUID = UUID
            .fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID DIS_UUID = UUID
            .fromString("0000180a-0000-1000-8000-00805f9b34fb");

    private boolean uartMode = false;
//    private boolean uartModeWaitingToConnect = false;

    BLEService tiService = null;
    private BluetoothDevice mDevice = null;
    private String mMacAdress = null;

    Queue<Pair<String, String>> readCharQueue = new LinkedList<Pair<String, String>>();
    boolean readingChar = false;

    public BluetoothDevice getDevice() {
        if (mDevice == null) {
            if (mMacAdress != null) {
                mDevice = AkylasBluetoothModule.getBTAdapter().getRemoteDevice(
                        mMacAdress);
            }
        }
        return mDevice;
    }

    public BLEDeviceProxy() {
        super();
        this.stopOnDestroy = true;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Class serviceClass() {
        return BLEService.class;
    }

    @Override
    public void unbindService() {
        super.unbindService();
        if (this.tiService != null) {
            this.tiService.setBLEServiceCb(null);
            this.tiService = null;
        }
        
    }

    protected void invokeBoundService() {
        this.tiService = (BLEService) this.service;
        this.tiService.setBLEServiceCb(this);
        this.tiService.connect(mMacAdress);
        super.invokeBoundService();
    }

    @Override
    public void handleCreationDict(HashMap dict, KrollProxy rootProxy)
    {
        setParentForBubbling(AkylasBluetoothModule.getInstance());
        super.handleCreationDict(dict, rootProxy);
        mMacAdress = TiConvert.toString(dict, "id", null);
        uartMode = TiConvert.toBoolean(dict, "uartMode", uartMode);
    }

    @Override
    protected void initActivity(Activity activity) {
        TiBaseActivity realActivity = (TiBaseActivity) TiApplication.getAppRootOrCurrentActivity();
        super.initActivity(realActivity);
        //we only destroy if main app activity is destroyed
        realActivity.addOnLifecycleEventListener(this);
    }

    // @Override
    // public void onResume(Activity activity) {
    // super.onResume(activity);
    // }

    // @Override
    // public void onPause(Activity activity) {
    // super.onPause(activity);
    // }

    @Override
    public void onDestroy(Activity activity) {
        super.onDestroy(activity);
        stop();
    }

    private void stop() {
        stopService();
        setState(AkylasBluetoothModule.STATE_DISCONNECTED);
    }

//    @Override
//    public void onDestroy(Activity activity) {
//        stop();
//        super.onDestroy(activity);
//    }

    @Override
    public String getApiName() {
        return "Ti.Bluetooth.BLEDevice";
    }

    private synchronized void setState(int state) {
        if (state != mState) {
            if (state == AkylasBluetoothModule.STATE_CONNECTED) {
                AkylasBluetoothModule.getInstance().onDeviceConnected(this);
                fireEvent("connected");
            } else if (state == AkylasBluetoothModule.STATE_DISCONNECTED) {
                if (mState == AkylasBluetoothModule.STATE_CONNECTING) {
                    fireError(-1, "failed to connect");
                } else {
                    AkylasBluetoothModule.getInstance().onDeviceDisconnected(this);
                    fireEvent("disconnected");
                }
                
            }
            mState = state;
        }
    }

    private void fireError(int code, String message) {
        if (hasListeners(TiC.EVENT_ERROR)) {
            KrollDict data = new KrollDict();
            data.putCodeAndMessage(code, message);
            fireEvent(TiC.EVENT_ERROR, data, true, false);
        }
    }

    public synchronized int getState() {
        return mState;
    }

    @Kroll.method
    @Kroll.getProperty
    public boolean getConnected() {
        return getState() == AkylasBluetoothModule.STATE_CONNECTED;
    }

    // @Kroll.method @Kroll.getProperty
    // public boolean getPaired() {
    // return mDevice != null && mDevice.getBondState() ==
    // BluetoothDevice.BOND_BONDED;
    // }
    //
    // @Kroll.method
    // public void pair() {
    // if (!getPaired()) {
    // AkylasBluetoothModule.pairDevice(mDevice);
    // }
    // }

    @Kroll.method
    public void connect() {
        if (mMacAdress == null) {
            fireError(-1, "no address supplied");
            return;
        }
        setState(AkylasBluetoothModule.STATE_CONNECTING);
        if (this.tiService == null) {
            startService();
        } else {
            this.tiService.connect(mMacAdress);
        }
    }

    private static UUID getUUIDFromString(final String uuid) {
        if (uuid.length() == 4) {
            return getUUIDFromString("0000" + uuid
                    + "-0000-1000-8000-00805f9b34fb");
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
    public void send(Object args) {
        if (this.tiService != null && uartMode) {
            byte[] bytes = TiConvert.toBytes(args);
            if (bytes != null) {
                BluetoothGattCharacteristic charac = this.tiService
                        .getCharacteristic(RX_SERVICE_UUID, TX_CHAR_UUID);
                tiService.writeCharacteristic(charac, bytes);
            }
        }

    }

    @Kroll.method
    public void startCharacteristicNotifications(final String serviceUUID,
            final String charUUID) {
        if (this.tiService != null) {
            BluetoothGattCharacteristic charac = this.tiService
                    .getCharacteristic(getUUIDFromString(serviceUUID),
                            getUUIDFromString(charUUID));
            this.tiService.setCharacteristicNotification(charac, true);
        }
    }

    @Kroll.method
    public void stopCharacteristicNotifications(final String serviceUUID,
            final String charUUID) {
        if (this.tiService != null) {
            BluetoothGattCharacteristic charac = this.tiService
                    .getCharacteristic(getUUIDFromString(serviceUUID),
                            getUUIDFromString(charUUID));
            this.tiService.setCharacteristicNotification(charac, false);
        }
    }

    @Kroll.method
    public void readCharacteristicValue(final String serviceUUID,
            final String charUUID) {
        if (readingChar) {
            readCharQueue.add(new Pair<String, String>(serviceUUID, charUUID));
            return;
        }
        if (this.tiService != null) {
            BluetoothGattCharacteristic charac = this.tiService
                    .getCharacteristic(getUUIDFromString(serviceUUID),
                            getUUIDFromString(charUUID));
            if (charac != null) {
                readingChar = true;
                this.tiService.readCharacteristic(charac);
            }
        }
    }

    @Kroll.method
    public void requestMTU(int mtu) {
        if (this.tiService != null) {
            this.tiService.requestMtu(mtu);
        }
    }
    
    @Kroll.method
    public void readRSSI() {
        if (this.tiService != null) {
            this.tiService.readRssi();
        }
    }

    @Kroll.method
    public void discoverServices() {
        if (this.tiService != null) {
            this.tiService.discoverServices();
        }
    }

    @Override
    public void onReadRemoteRssi(int rssi) {
        if (hasListeners("rssi", false)) {
            KrollDict data = new KrollDict();
            data.put("rssi", rssi);
            fireEvent("rssi", data, false, false);
        }
    }

    @Override
    public void onMtuChanged(int mtu) {
        this.setPropertyAndFire("mtu", mtu);
        if (hasListeners("mtu", false)) {
            KrollDict data = new KrollDict();
            data.put("mtu", mtu);
            fireEvent("mtu", data, false, false);
        }
    }

    @Override
    public void notifyConnectedGATT() {
        if (TiC.LOLLIPOP_OR_GREATER) {
            int mtu = TiConvert.toInt(getProperty("mtu"), -1);
            if (mtu >= 0) {
                this.tiService.requestMtu(mtu);
            }
        }
        if (!uartMode) {
            setState(AkylasBluetoothModule.STATE_CONNECTED);
        } else {
            discoverServices();
        }
    }

    @Override
    public void notifyDisconnectedGATT() {
        setState(AkylasBluetoothModule.STATE_DISCONNECTED);
    }

    @Override
    public void onServicesDiscovered(int status, List<String> serviceNames) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            fireError(status, "error while discovering services");
            stop();
            return;
        }
        if (hasListeners("discoveredServices", false)) {
            HashMap data = new HashMap<>();
            data.put("services", serviceNames.toArray());
            fireEvent("discoveredServices", data, false, false);
        }
        if (hasListeners("discoveredCharacteristics", false)) {
            List<BluetoothGattService> services = this.tiService.getServices();
            if (services != null)
            for(BluetoothGattService service : services) {
                HashMap data = new HashMap<>();
                data.put("service", stringFromUUID(service.getUuid()));
                List<String> chararcteristicNames = new ArrayList<>();
                for(BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    chararcteristicNames.add(stringFromUUID(characteristic.getUuid()));
                }
                data.put("characteristics", chararcteristicNames.toArray());
                fireEvent("discoveredCharacteristics", data, false, false);
            }
            
        }
        if (serviceNames.contains(RX_SERVICE_UUID.toString())) {
//            uartModeWaitingToConnect = true;
            BluetoothGattCharacteristic charac = this.tiService
                    .getCharacteristic(RX_SERVICE_UUID, RX_CHAR_UUID);
            this.tiService.setCharacteristicNotification(charac, true);
//            charac = this.tiService
//                    .getCharacteristic(RX_SERVICE_UUID, TX_CHAR_UUID);
//            charac.setWriteType(BluetoothGattCharacteristic.PROPERTY_WRITE);
        }
        if (uartMode) {
            setState(AkylasBluetoothModule.STATE_CONNECTED);
//             charac = this.tiService.getCharacteristic(DIS_UUID,
            // FIRMWARE_REVISON_UUID);
            // this.tiService.readCharacteristic(charac);
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

    @Override
    public void onCharacteristicRead(
            BluetoothGattCharacteristic characteristic, byte[] bytes) {
        long timestamp = (new Date()).getTime();
        int length = bytes.length;
        final String eventType = "read";
        if (length >= 0 && hasListeners(eventType, false)) {
            KrollDict data = new KrollDict();
            data.put(TiC.PROPERTY_TIMESTAMP, timestamp);
            data.put(TiC.PROPERTY_LENGTH, length);
            data.put("service", stringFromUUID(characteristic.getService()
                    .getUuid()));
            data.put("characteristic", stringFromUUID(characteristic.getUuid()));
            data.put(TiC.PROPERTY_DATA, TiBlob.blobFromObject(bytes));
            fireEvent(eventType, data, false, false);
        }
        readingChar = false;
        if (readCharQueue.size() > 0) {
            Pair<String, String> value = readCharQueue.poll();
            readCharacteristicValue(value.first, value.second);
        }
    }

    @Override
    public void onDescriptorRead(BluetoothGattDescriptor descriptor, byte[] bytes) {
        long timestamp = (new Date()).getTime();
        int length = bytes.length;
        final String eventType = "read";
        if (length >= 0 && hasListeners(eventType, false)) {
            KrollDict data = new KrollDict();
            data.put(TiC.PROPERTY_TIMESTAMP, timestamp);
            data.put(TiC.PROPERTY_LENGTH, length);
            data.put("service", stringFromUUID(descriptor.getCharacteristic().getService()
                    .getUuid()));
            data.put("characteristic", stringFromUUID(descriptor.getCharacteristic().getUuid()));
            data.put("descriptor", stringFromUUID(descriptor.getUuid()));
            data.put(TiC.PROPERTY_DATA, TiBlob.blobFromObject(bytes));
            fireEvent(eventType, data, false, false);
        }
        readingChar = false;
        if (readCharQueue.size() > 0) {
            Pair<String, String> value = readCharQueue.poll();
            readCharacteristicValue(value.first, value.second);
        }
    }   

    @Override
    public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            fireError(status, "error while writing descriptor");
//            if (uartModeWaitingToConnect) {
//                uartModeWaitingToConnect = false;
                stop();
//            }
        } else {
//            if (uartModeWaitingToConnect) {
//                uartModeWaitingToConnect = false;
//                setState(AkylasBluetoothModule.STATE_CONNECTED);
//            }
        }
        
    }
    
    @Override
    public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
        KrollDict data = null;
        if (status != BluetoothGatt.GATT_SUCCESS) {
            data = new KrollDict();
            data.putCodeAndMessage(status, "error while writing characteristic");
//            fireError(status, "error while writing characteristic");
//            if (uartModeWaitingToConnect) {
//                uartModeWaitingToConnect = false;
//                stop();
//            }
        } else {
//            if (uartModeWaitingToConnect) {
//                uartModeWaitingToConnect = false;
//                setState(AkylasBluetoothModule.STATE_CONNECTED);
//            }
        }
        fireEvent("didWrite", data, false, true);
        
    }
}
