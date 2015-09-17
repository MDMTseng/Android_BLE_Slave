package me.mamx.ble_slave;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

/**
 * Created by ctseng on 9/14/15.
 */
public class HeartRateGattServices {


    public static final UUID HEART_RATE_SERVICE_UUID = UUID
            .fromString("0000180D-0000-1000-8000-00805f9b34fb");



    public static final UUID HEART_RATE_MEASUREMENT_UUID = UUID
            .fromString("00002A37-0000-1000-8000-00805f9b34fb");
    public static final UUID BODY_SENSOR_LOCATION_UUID = UUID
            .fromString("00002A38-0000-1000-8000-00805f9b34fb");
    public static final UUID HEART_RATE_CONTROL_POINT_UUID = UUID
            .fromString("00002A39-0000-1000-8000-00805f9b34fb");


    public static BluetoothGattService BuildService()
    {
        BluetoothGattService mHeartRateService= new BluetoothGattService(HEART_RATE_SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic chara=new BluetoothGattCharacteristic(HEART_RATE_MEASUREMENT_UUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,0);
        chara.setValue("HEART_RATE");

        mHeartRateService.addCharacteristic(chara);

        chara=new BluetoothGattCharacteristic(BODY_SENSOR_LOCATION_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ,BluetoothGattCharacteristic.PERMISSION_READ);
        chara.setValue("SENSOR_LOCA");


        mHeartRateService.addCharacteristic(chara);


        chara=new BluetoothGattCharacteristic(
                HEART_RATE_CONTROL_POINT_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE|BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_WRITE|BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM);

        chara.setValue("CONTROL_POINT");
        mHeartRateService.addCharacteristic(chara);



        return mHeartRateService;
    }

}
