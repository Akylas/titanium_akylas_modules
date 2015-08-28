package akylas.bluetooth;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import org.appcelerator.kroll.common.Log;
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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;


public class BLEService extends TiEnhancedService {
    private final static String TAG = BLEService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    BluetoothGatt mBluetoothGatt;
    private BluetoothDevice mDevice;
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
    
    
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case READ_RSSI_REPEAT:
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.readRemoteRssi();
//                sendMessageDelayed(obtainMessage(READ_RSSI_REPEAT), READING_RSSI_TASK_FREQUENCY);
                }
                break;
            }
        }
    };
    
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
            case BluetoothDevice.ACTION_NAME_CHANGED:
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mDevice == device) {
                    if (mBLEServiceCb != null) {
                        mBLEServiceCb.nameChanged(intent.getStringExtra(BluetoothDevice.EXTRA_NAME));
                    }
                }
                break;
            }

        }
    };

    public void readRssi() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.readRemoteRssi();
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

                Log.d(TAG,"Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.d(TAG,"Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (mBLEServiceCb != null) {
                    mBLEServiceCb.notifyDisconnectedGATT();
                }
                Log.d(TAG,"Disconnected from GATT server.");
            }
            mConnectionState = newState;
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG,"onServicesDiscovered status = " + status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (mBLEServiceCb != null) {
                    mBLEServiceCb.onServicesDiscovered();
                }
                for (BluetoothGattService service : gatt.getServices()) {
                    Log.d(TAG,"onServicesDiscovered : " + service.getUuid().toString());
                }
            } else {
                Log.d(TAG,"onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                displayCharacteristic(characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            writing = false;
            if (writeQueue.size() > 0) {
                writeCharacteristic(characteristic, writeQueue.poll());
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            displayCharacteristic(characteristic);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (mBLEServiceCb != null) {
                mBLEServiceCb.displayRssi(rssi);
            }
        }
    };

    public void setBLEServiceCb(BLEServiceCallback cb) {
        if (cb != null) {
            mBLEServiceCb = cb;
        }
    }

    private void displayCharacteristic(final BluetoothGattCharacteristic characteristic) {
        if (mBLEServiceCb != null) {
            mBLEServiceCb.onData(characteristic, characteristic.getValue());
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

    public boolean initialize() {
        if (TiC.JELLY_BEAN_MR2_OR_GREATER) {
            if (mBluetoothManager == null) {
                mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                if (mBluetoothManager != null) {
                    mBluetoothAdapter = mBluetoothManager.getAdapter();
               }
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

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device. Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = BluetoothProfile.STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        mDevice = mBluetoothAdapter.getRemoteDevice(address);
        if (mDevice == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
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
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        mDevice = null;
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
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothGatt == null || characteristic == null) {
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

    /**
     * Requst a write on a give {@code BluetoothGattCharacteristic}. The write
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicWrite(andorid.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null || characteristic == null) {
            return;
        }
        if (writing) {
            Log.d(TAG, "queuing " + new String(value));
            writeQueue.add(value);
        }
        writing = true;
        characteristic.setValue(value);
        boolean status = mBluetoothGatt.writeCharacteristic(characteristic);
        Log.d(TAG, "writeCharacteristic - status=" + status);  
    }
    
    /**
     * Requst a write on a give {@code BluetoothGattCharacteristic}. The write
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicWrite(andorid.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null || characteristic == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
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
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
        if (descriptor != null) {
            descriptor.setValue(enabled?BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE:BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
        
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
        public void displayRssi(int rssi);

        public void onData(final BluetoothGattCharacteristic characteristic, byte[] data);

        public void notifyConnectedGATT();

        public void notifyDisconnectedGATT();

        public void onServicesDiscovered();
        public void nameChanged(final String name);

     }
}
