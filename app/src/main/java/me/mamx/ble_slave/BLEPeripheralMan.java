package me.mamx.ble_slave;

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
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by ctseng on 9/18/15.
 */
public class BLEPeripheralMan {

    long GetCharaM32bit(BluetoothGattCharacteristic chara)
    {
        long KK=chara.getUuid().getMostSignificantBits()>>32;
        return (KK)&(0xFFFFFFFFL);
    }
    private CommCH_IF UICommIF=null;
    private BLEInterCommIf BTCommIF=null;
    public class BLEInterCommIf extends CommCH_IF {


        @Override
        public boolean RecvData(Object CH, byte[] data)//From UI
        {
            BluetoothGattCharacteristic chara=GattCharaMap.get(CH);
            chara.setValue(data);
            sendNotificationToDevices(chara);
            Log.v("BLEPeri..RecvData",data.toString());
            return true;
        }




        @Override
        public boolean SendData(Object CH,byte[] data){//to BLE

            Log.v("BLEPeri..SendData",data.toString());
            BluetoothGattCharacteristic chara=(BluetoothGattCharacteristic)CH;
            return UICommIF.RecvData(GetCharaM32bit(chara),data);
        }
    }
    BLEPeripheralMan thisBLEMan=this;

    private static final String TAG =BLEPeripheralMan.class.getCanonicalName();
    private HashSet<BluetoothDevice> mBluetoothDevices=null;
    private BluetoothManager mBluetoothManager=null;
    private BluetoothAdapter mBluetoothAdapter=null;
    private BluetoothLeAdvertiser mAdvertiser;

    HashMap <Long,BluetoothGattCharacteristic>GattCharaMap=null;




    JsonBLEProfileBuilder jBp=null;
    Context context=null;


    public BLEPeripheralMan(Context context)  {
        this.context=context;
        mBluetoothManager= (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothDevices = new HashSet<BluetoothDevice>();
        BTCommIF=new BLEInterCommIf();
    }
    public BLEPeripheralMan(Context context,String JsonProfile) throws JSONException {
        //this(context);


        this.context=context;
        mBluetoothManager= (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothDevices = new HashSet<BluetoothDevice>();
        BTCommIF=new BLEInterCommIf();

        ResetByJsonProf(JsonProfile);
    }


    private BluetoothGattServer mGattServer=null;
    private void ReBuildGatt()  {
        StopAdvertising();
        if(mGattServer!=null)mGattServer.close();

        if(GattCharaMap!=null)GattCharaMap.clear();
        else GattCharaMap=new  HashMap ();
        mGattServer = mBluetoothManager.openGattServer(context, mGattServerCallback);

    }
    public void ResetByJsonProf(String JsonProfile) throws JSONException {
        ReBuildGatt();
        AddJsonProf(JsonProfile);
    }
    public void ResetByJsonProf(JSONObject JsonProfile) throws JSONException {
        ReBuildGatt();
        AddJsonProf(JsonProfile);
    }
    public void AddJsonProf(String JsonProfile) throws JSONException {
        AddJsonProf(new JSONObject(JsonProfile));
    }
    public void AddJsonProf(JSONObject JsonProfile) throws JSONException {
        jBp=new JsonBLEProfileBuilder(JsonProfile);
        BuildGattServer(jBp);
    }


    CommCH_IF getCommIf()
    {
        return BTCommIF;
    }



    void BuildGattServer(JsonBLEProfileBuilder jsonProfBuilder) throws JSONException {

        if(jsonProfBuilder!=null)jsonProfBuilder.BuildProfile(mGattServer);


        GattCharaMap.clear();
        List<BluetoothGattService> services=mGattServer.getServices();
        for(BluetoothGattService service:services)
            for (BluetoothGattCharacteristic chara:service.getCharacteristics())
            {
                GattCharaMap.put(GetCharaM32bit(chara),chara);

            }

    }

    void StartAdvertising()
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
                .build();
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        mAdvertiser.startAdvertising(mAdvSettings, mAdvData, mAdvCallback);


    }
    void StopAdvertising()
    {

        if(mAdvertiser!=null)mAdvertiser.stopAdvertising(mAdvCallback);
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

        }
    };



    public void sendNotificationToDevices(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothDevices.isEmpty()) {
            Toast.makeText(context, "bluetoothDeviceNotConnected", Toast.LENGTH_SHORT).show();
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
            //Log.v(TAG, "Characteristic Write request: " + Arrays.toString(value));
            BTCommIF.SendData(characteristic, value);
            int status = thisBLEMan.writeCharacteristic(characteristic, offset, value);
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

        return BluetoothGatt.GATT_SUCCESS;
    }


    void setUICommIf(CommCH_IF UICommIF)
    {
        this.UICommIF=UICommIF;
    }


}
