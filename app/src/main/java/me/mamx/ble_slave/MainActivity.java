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
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {
    MainActivity MainAct=this;
    private static final String TAG = MainActivity.class.getCanonicalName();
    boolean isSupportBLESlave=false;
    //TextView TV=null;

    BLEPeripheralMan BLEMan=null;

    WebUIMan WU=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TV=(TextView)findViewById(R.id.text);
        isSupportBLESlave=((BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE))
                .getAdapter().isMultipleAdvertisementSupported();
        //TV.setText(((Boolean)isSupportBLESlave).toString());
        if(!isSupportBLESlave)return;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        AssetManager am = this.getAssets();
        String JsonCon=null;
        try {
            InputStream input = am.open("BLEProfile_GoProCompatibleDev.json");
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();
            JsonCon=new String(buffer);


            //TV.setText(JsonCon);
        } catch (IOException e) {
            e.printStackTrace();
        }



        BLEMan=new BLEPeripheralMan(this);


        WU=new WebUIMan((WebView)findViewById(R.id.webView_mainUI),"file:///android_asset/MainUI/index.html"){
            @Override
            public void UIDataRecvCB(String type,JSONObject jsonData)
            {
                if(type.contentEquals("gattProfileJson"))
                {
                    try {
                        BLEMan.ResetByJsonProf(jsonData.getJSONObject("data"));
                        BLEMan.StartAdvertising();
                        //BLEMan.
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }



            }
        };


        BLEMan.setUICommIf(WU.getCommIf());

        WU.setBTCommIf(BLEMan.getCommIf());




    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //exe shortcircuit
        return WU.OnKeyDown(keyCode, event)|| super.onKeyDown(keyCode, event);
    }


}


