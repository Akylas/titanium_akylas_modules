package akylas.bluetooth;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiEnhancedService;
import org.appcelerator.titanium.proxy.TiEnhancedServiceProxy;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;


public class BLEService extends TiEnhancedService {
    private final static String TAG = BLEService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    BluetoothGatt mBluetoothGatt;
//    private BluetoothDevice mDevice;
    private int mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
    private BLEServiceCallback mBLEServiceCb = null;

    private static final int READ_RSSI_REPEAT = 1;
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    
    Queue<byte[]> writeQueue = new LinkedList<byte[]>();
    boolean writing = false;
    
    @Override
    protected void bindToProxy(final TiEnhancedServiceProxy proxy) {
        super.bindToProxy(proxy);
    }
    
    
//    private final Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//            case READ_RSSI_REPEAT:
//                if (mBluetoothGatt != null) {
//                    mBluetoothGatt.readRemoteRssi();
////                sendMessageDelayed(obtainMessage(READ_RSSI_REPEAT), READING_RSSI_TASK_FREQUENCY);
//                }
//                break;
//            }
//        }
//    };
    
    public void readRssi() {
        if (mBluetoothGatt != null) {
            if (!TiApplication.isUIThread()) {
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothGatt.readRemoteRssi();
                    }
                });
                return;
            }
            mBluetoothGatt.readRemoteRssi();
        }
    }

    public void discoverServices() {
        if (mBluetoothGatt != null) {
            if (!TiApplication.isUIThread()) {
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothGatt.discoverServices();
                    }
                });
                return;
            }
            mBluetoothGatt.discoverServices();
        }
    }
    // Implements callback methods for GATT events that the app cares about. For
    // example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG,"onConnectionStateChange status = " + status + ", newState = " + newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (mBLEServiceCb != null) {
                    mBLEServiceCb.notifyConnectedGATT();
                }

                // Attempts to discover services after successful connection.
//                Log.d(TAG,"Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (mConnectionState == BluetoothProfile.STATE_CONNECTING) {
                    //this is an android bug? try again!
                    connect(mBluetoothDeviceAddress);
                    return;
                }
                if (mBLEServiceCb != null) {
                    mBLEServiceCb.notifyDisconnectedGATT();
                }
                Log.d(TAG,"Disconnected from GATT server.");
//                close();
            }
            mConnectionState = newState;
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG,"onServicesDiscovered status = " + status);
            
            
            List<String> services = new ArrayList<>();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                for (BluetoothGattService service : gatt.getServices()) {
                    services.add(service.getUuid().toString());
                }
            }
            if (mBLEServiceCb != null) {
                mBLEServiceCb.onServicesDiscovered(status, services);
            }
            
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (mBLEServiceCb != null) {
                    mBLEServiceCb.onCharacteristicRead(characteristic, characteristic.getValue());
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            writing = false;
            if (writeQueue.size() > 0) {
                writeCharacteristic(characteristic, writeQueue.poll());
            } else {
                if (mBLEServiceCb != null) {
                    mBLEServiceCb.onCharacteristicWrite(characteristic, status);
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                int status) {
            if (mBLEServiceCb != null) {
                mBLEServiceCb.onDescriptorWrite(descriptor, status);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (mBLEServiceCb != null) {
                    mBLEServiceCb.onDescriptorRead(descriptor, descriptor.getValue());
                }
            }
        }
        
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (mBLEServiceCb != null) {
                mBLEServiceCb.onCharacteristicRead(characteristic, characteristic.getValue());
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (mBLEServiceCb != null) {
                mBLEServiceCb.onReadRemoteRssi(rssi);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            if (mBLEServiceCb != null) {
                mBLEServiceCb.onMtuChanged(mtu);
            }
        }
    };

    public void setBLEServiceCb(BLEServiceCallback cb) {
        if (cb != null) {
            mBLEServiceCb = cb;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mBluetoothAdapter == null) {
            initialize();
        }
        return super.onBind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }
    @Override
    public void onDestroy() {
        close();
        super.onDestroy();
    }
    

    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        }
        if (TiC.JELLY_BEAN_MR2_OR_GREATER) {
            
            if (mBluetoothManager != null) {
                mBluetoothAdapter = mBluetoothManager.getAdapter();
            }
        } else {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    Handler handler = new Handler();
    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }
    public boolean connect(final String address) {

        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        if (!TiApplication.isUIThread()) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connect(address);
                }
            });
            return true;
        }

        // Previously connected device. Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            try {
                if (mBluetoothGatt.connect()) {
                    mConnectionState = BluetoothProfile.STATE_CONNECTING;
                    return true;
                } else {
                    return false;
                }
            } catch(Exception e) {
                stop();
                return false;
            }
            
        }
        if (mBluetoothGatt != null) {
            close();
        }
        BluetoothDevice mDevice = mBluetoothAdapter.getRemoteDevice(address);
        if (mDevice == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        List<BluetoothDevice> connectedDevices = mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
        if (connectedDevices.contains(mDevice)) {
            Log.w(TAG, "Device already connected....");
       }
        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
        mBluetoothGatt = mDevice.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = BluetoothProfile.STATE_CONNECTING;
        return true;
    }

    public void disconnect() {
        if (mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public synchronized void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
//        mDevice = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     * 
     * @param characteristic
     *            The characteristic to read from.
     */
    public void readCharacteristic(final BluetoothGattCharacteristic characteristic) {
        if (mBluetoothGatt == null || characteristic == null) {
            return;
        }
        if (!TiApplication.isUIThread()) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    readCharacteristic(characteristic);
                }
            });
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }
    
    public BluetoothGattCharacteristic getCharacteristic(final UUID serviceUUID, final UUID characteristicUUID) {
        BluetoothGattService service = mBluetoothGatt.getService(serviceUUID);
        if (service == null) {
            return null;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
        return characteristic;
    }
    
    public List<BluetoothGattService> getServices() {
        if (mBluetoothGatt == null) {
            return null;
        }
        return  mBluetoothGatt.getServices();
    }

    /**
     * Requst a write on a give {@code BluetoothGattCharacteristic}. The write
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicWrite(andorid.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     */
    public void writeCharacteristic(final BluetoothGattCharacteristic characteristic, final byte[] value) {
        if (mBluetoothGatt == null || characteristic == null) {
            return;
        }
        if (writing) {
            Log.d(TAG, "queuing " + new String(value));
            writeQueue.add(value);
        }
        if (!TiApplication.isUIThread()) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    writeCharacteristic(characteristic, value);
                }
            });
            return;
        }
        writing = true;
        characteristic.setValue(value);
        boolean status = mBluetoothGatt.writeCharacteristic(characteristic);
        Log.d(TAG, "writeCharacteristic - status=" + status);  
        if (!status) {
            if (mBLEServiceCb != null) {
                mBLEServiceCb.onCharacteristicWrite(characteristic, -1);
            }
        }
    }
    
    /**
     * Requst a write on a give {@code BluetoothGattCharacteristic}. The write
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicWrite(andorid.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     */
    public void writeCharacteristic(final BluetoothGattCharacteristic characteristic) {
        if (mBluetoothGatt == null || characteristic == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (!TiApplication.isUIThread()) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    writeCharacteristic(characteristic);
                }
            });
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }


    /**
     * Enables or disables notification on a give characteristic.
     * 
     * @param characteristic
     *            Characteristic to act on.
     * @param enabled
     *            If true, enable notification. False otherwise.
     */
    public void setCharacteristicNotification(final BluetoothGattCharacteristic characteristic, final boolean enabled) {
        if (mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        
        if (!TiApplication.isUIThread()) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setCharacteristicNotification(characteristic, enabled);
                }
            });
//            FutureTask<Boolean> futureResult = new FutureTask<Boolean>(new Callable<Boolean>() {
//                @Override
//                public Boolean call() throws Exception {
//                    return setCharacteristicNotification(characteristic, enabled);
//                }
//            }); 
//            // this block until the result is calculated!
//            try {
//                this.runOnUiThread(futureResult);
//                return futureResult.get();
//            } catch (Exception e) {
//                return false;
//            }
        }
        boolean status = mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        if (status) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
            if (descriptor != null) {
                status = false;
                byte[] currentValue = descriptor.getValue();
                if (currentValue != BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) {
                    descriptor.setValue(enabled?BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE:BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    status = mBluetoothGatt.writeDescriptor(descriptor);
                } else {
                    //still call the delegate so that it can handle what it want(uart connect)
                    if (mBLEServiceCb != null) {
                        mBLEServiceCb.onDescriptorWrite(descriptor, 0);
                    }
                }
                
            }
        }
//        return status;
        
    }
    
    public void requestMtu(int mtu) {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.requestMtu(mtu);
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This
     * should be invoked only after {@code BluetoothGatt#discoverServices()}
     * completes successfully.
     * 
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null)
            return null;

        return mBluetoothGatt.getServices();
    }
    
    public int getState() {
        return mConnectionState;
    }

    public interface BLEServiceCallback {
        public void onReadRemoteRssi(int rssi);
        public void onMtuChanged(int mtu);
     

        public void onCharacteristicRead(final BluetoothGattCharacteristic characteristic, byte[] data);
        public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status);
        public void onDescriptorRead(final BluetoothGattDescriptor descriptor, byte[] data);
        public void onDescriptorWrite(final BluetoothGattDescriptor descriptor, int status);

        public void notifyConnectedGATT();

        public void notifyDisconnectedGATT();

        public void onServicesDiscovered(int status, List<String> services);
        public void nameChanged(final String name);

     }



}
