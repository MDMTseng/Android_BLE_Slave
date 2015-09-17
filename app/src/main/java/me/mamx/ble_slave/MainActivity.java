package me.mamx.ble_slave;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import android.widget.Toast;

public class MainActivity extends Activity {
    MainActivity MainAct=this;
    private static final String TAG = MainActivity.class.getCanonicalName();
    boolean isSupportBLESlave=false;
    TextView TV=null;
    private HashSet<BluetoothDevice> mBluetoothDevices=null;
    private BluetoothManager mBluetoothManager=null;
    private BluetoothAdapter mBluetoothAdapter=null;
    private BluetoothLeAdvertiser mAdvertiser;



    private ArrayList<BluetoothGattCharacteristic> NotiCharas=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TV=(TextView)findViewById(R.id.text);
        isSupportBLESlave=((BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE))
                .getAdapter().isMultipleAdvertisementSupported();
        TV.setText(((Boolean)isSupportBLESlave).toString());

        NotiCharas=new ArrayList<BluetoothGattCharacteristic>();
        BLEInit();
        BuildGattServer();
        BuildAdv(HeartRateGattServices.HEART_RATE_SERVICE_UUID);





        notiTimerHandler.postDelayed(notiTimerRunnable,1000);

    }

    private Handler notiTimerHandler = new Handler( );
    private Runnable notiTimerRunnable = new Runnable( ) {
        public void run ( ) {
            for (BluetoothGattCharacteristic chara:NotiCharas)
            {
                sendNotificationToDevices(chara);
            }
            notiTimerHandler.postDelayed(this,3000);
        }
    };





    void BLEInit()
    {

        mBluetoothDevices = new HashSet<BluetoothDevice>();
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }



    private BluetoothGattServer mGattServer;

    void BuildGattServer()
    {
        mGattServer = mBluetoothManager.openGattServer(this, mGattServerCallback);

        BluetoothGattService GATTServ=HeartRateGattServices.BuildService();

        List<BluetoothGattCharacteristic> charas=GATTServ.getCharacteristics();
        mGattServer.addService(GATTServ);

        for (BluetoothGattCharacteristic chara:charas)
        {
            if((chara.getProperties()&
                (BluetoothGattCharacteristic.PROPERTY_INDICATE|BluetoothGattCharacteristic.PROPERTY_NOTIFY))!=0)
            NotiCharas.add(chara);
        }

    }

    void BuildAdv(UUID uuid)
    {
        AdvertiseSettings mAdvSettings=null;
        mAdvSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .build();
        AdvertiseData mAdvData=null;
        mAdvData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(true)
                .addServiceUuid(new ParcelUuid(uuid))
                .build();

        mAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        mAdvertiser.startAdvertising(mAdvSettings, mAdvData, mAdvCallback);



    }

    private final AdvertiseCallback mAdvCallback = new AdvertiseCallback() {
        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.e(TAG, "Not broadcasting: " + errorCode);
            String statusText = null;
            switch (errorCode) {
                case ADVERTISE_FAILED_ALREADY_STARTED:
                    //statusText = R.string.status_advertising;
                    Log.w(TAG, "App was already advertising");
                    break;
                case ADVERTISE_FAILED_DATA_TOO_LARGE:
                    statusText = "status_advDataTooLarge";
                    break;
                case ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                    statusText = "status_advFeatureUnsupported";
                    break;
                case ADVERTISE_FAILED_INTERNAL_ERROR:
                    statusText = "status_advInternalError";
                    break;
                case ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                    statusText = "status_advTooManyAdvertisers";
                    break;
                default:
                    statusText = "status_notAdvertising";
            }
            TV.setText(statusText);

        }
    };



    public void sendNotificationToDevices(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothDevices.isEmpty()) {
            Toast.makeText(this, "bluetoothDeviceNotConnected", Toast.LENGTH_SHORT).show();
        } else {
            boolean indicate = (characteristic.getProperties()
                    & BluetoothGattCharacteristic.PROPERTY_INDICATE)
                    == BluetoothGattCharacteristic.PROPERTY_INDICATE;
            for (BluetoothDevice device : mBluetoothDevices) {
                // true for indication (acknowledge) and false for notification (unacknowledge).
                mGattServer.notifyCharacteristicChanged(device, characteristic, indicate);
            }
        }
    }





    private final BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, final int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    mBluetoothDevices.add(device);
                    //updateConnectedDevicesStatus();
                    Log.v(TAG, "Connected to device: " + device.getAddress());
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    mBluetoothDevices.remove(device);
                    //updateConnectedDevicesStatus();
                    Log.v(TAG, "Disconnected from device");
                }
            } else {
                mBluetoothDevices.remove(device);
               // updateConnectedDevicesStatus();
                // There are too many gatt errors (some of them not even in the documentation) so we just
                // show the error to the user.
                final String errorMessage = "status_errorWhenConnecting" + ": " + status;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
                Log.e(TAG, "Error when connecting: " + status);
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.d(TAG, "Device tried to read characteristic: " + characteristic.getUuid());
            Log.d(TAG, "Value: " + Arrays.toString(characteristic.getValue()));
            if (offset != 0) {
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset,
            /* value (optional) */ null);
                return;
            }
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
                    offset, characteristic.getValue());
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            Log.v(TAG, "Notification sent. Status: " + status);
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                 BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
                                                 int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite,
                    responseNeeded, offset, value);
            Log.v(TAG, "Characteristic Write request: " + Arrays.toString(value));
            int status = MainAct.writeCharacteristic(characteristic, offset, value);
            if (responseNeeded) {
                mGattServer.sendResponse(device, requestId, status,
            /* No need to respond with an offset */ 0,
            /* No need to respond with a value */ null);
            }
        }


    };
    public int writeCharacteristic(BluetoothGattCharacteristic characteristic, int offset, byte[] value) {
        if (offset != 0) {
            return BluetoothGatt.GATT_INVALID_OFFSET;
        }

        characteristic.setValue(value);

        if ((value[0] & 1) == 1) {
            MainAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }
        return BluetoothGatt.GATT_SUCCESS;
    }
}


