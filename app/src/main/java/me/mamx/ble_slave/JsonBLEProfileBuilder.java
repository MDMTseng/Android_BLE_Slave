package me.mamx.ble_slave;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by ctseng on 9/17/15.
 */
public class JsonBLEProfileBuilder {


    JSONObject jObject=null;
    public static String GP_XX (String SubUUID_32b)
    {
        if(SubUUID_32b.length()!=4)return null;
        return "b5f9"+SubUUID_32b+"-aa8d-11e3-9046-0002a5d5c51b";
    }

    public static String UUID16b (String SubUUID_32b)
    {
        if(SubUUID_32b.length()!=4)return null;
        return "0000"+SubUUID_32b+"-0000-1000-8000-00805f9b34fb";
    }

    public static String UUID128b_NoDash (String SubUUID_128b)
    {
        if(SubUUID_128b.length()!=32)return null;
        return SubUUID_128b.substring(0,7)+"-"+SubUUID_128b.substring(8,11)+"-"+SubUUID_128b.substring(12,15)+"-"+SubUUID_128b.substring(16,19)+"-"+SubUUID_128b.substring(20,31);
    }

    public static UUID CompleteUUID(String sUUID)
    {
        if(sUUID.startsWith("0x"))
            sUUID= UUID16b(sUUID.substring(2));
        else if(sUUID.length()==4)
            sUUID= UUID16b(sUUID);
        else if(sUUID.startsWith("GP-"))
            sUUID= GP_XX(sUUID.substring(3));
        else if(sUUID.length()==32)
            sUUID= UUID128b_NoDash(sUUID);

        try
        {
            return UUID.fromString(sUUID);
        }
        catch(Exception e)
        {
            return null;
        }
    }
    public static String CompleteUUIDStr(String sUUID)
    {
        UUID uuid=CompleteUUID(sUUID);

        if(uuid==null)return null;

        return uuid.toString();


    }

    public JsonBLEProfileBuilder(JSONObject gattTable_Json)
    {
        SetJson(gattTable_Json);
        //jObject = new JSONObject();
    }
    public void SetJson(JSONObject gattTable_Json)
    {

        jObject=gattTable_Json;
    }

    public boolean BuildProfile(BluetoothGattServer GattServer) throws JSONException {
        return BuildProfile(GattServer, jObject);
    }

    String ver=null;

    public boolean BuildProfile(BluetoothGattServer GattServer,JSONObject jObject) throws JSONException {
        this.jObject=jObject;

        JSONObject jProfile=null;
        try {
            if(jObject==null)throw new JSONException("null Json");

            ver=jObject.optString("BLEProJsonVer");
            if(ver==null)throw new JSONException("null BLEProJsonVer");


            jProfile=jObject.optJSONObject("profile");
            if(jProfile==null)throw new JSONException("null Profile object");

            JSONArray jservices=jProfile.optJSONArray("services");
            if(jservices==null)throw new JSONException("null Profile services");


            ArrayList<BluetoothGattService> serviceList=new ArrayList<BluetoothGattService>();
            //jObject.getJSONObject();
            for(int i=0;i<jservices.length();i++)
                serviceList.add(BuildService(jservices.getJSONObject(i)));

            for(BluetoothGattService serv:serviceList)
                GattServer.addService(serv);

            return true;
        } catch (JSONException e) {


            e.printStackTrace();
            ver=null;
            throw new JSONException("Json BLE profile formate Error!!");
        }
    }


    public static BluetoothGattService BuildService(JSONObject jService)throws JSONException {

        if(jService==null)throw new JSONException("null Service");

        String uuidStr=jService.optString("uuid");

        UUID uuid=CompleteUUID(uuidStr);
        if(uuid==null)throw new JSONException("wrong service UUID");


        String id=jService.optString("id");
        String description=jService.optString("description");


        JSONArray jcharas=jService.optJSONArray("characteristics");
        if(jcharas==null)throw new JSONException("null Service charas");


        BluetoothGattService Service= new BluetoothGattService(uuid,BluetoothGattService.SERVICE_TYPE_PRIMARY);
        //Service.
        //jObject.getJSONObject();
        for(int i=0;i<jcharas.length();i++)
        {
            Service.addCharacteristic(BuildChara(jcharas.getJSONObject(i)));

        }

        Log.v("BuildService","======================");




        return Service;
    }
    public static BluetoothGattCharacteristic BuildChara(JSONObject jChara)throws JSONException {

        if(jChara==null)throw new JSONException("null Characteristic");

        String uuidStr=jChara.getString("uuid");

        UUID uuid=CompleteUUID(uuidStr);
        if(uuid==null)throw new JSONException("wrong chara UUID");


        String id=jChara.optString("id");
        String description=jChara.optString("description");

        JSONArray jproperties=jChara.optJSONArray("properties");




        JSONArray jpermissions=jChara.optJSONArray("permissions");

        int properties=0;
        int permissions=0;
        if(jproperties!=null)for(int i=0;i<jproperties.length();i++)
        {
            properties|= parsePropertyNameForChar(jproperties.getString(i));

        }
        if(properties==0)properties=BluetoothGattCharacteristic.PROPERTY_READ;
        permissions=PermissionRecommendationNameFromCharProperty(properties);

        if(jpermissions!=null) {
            permissions=0;
            for (int i = 0; i < jpermissions.length(); i++) {
                permissions |= parsePermissionNameForChar(jpermissions.getString(i));
            }
        }




        String length=jChara.optString("length");

        String value=jChara.optString("value");



        BluetoothGattDescriptor de;


        BluetoothGattCharacteristic chara=new BluetoothGattCharacteristic(uuid,
                properties,permissions);

        if(value!=null)chara.setValue(value);
        //chara.setWriteType();

        /*JSONArray jdescri=jChara.optJSONArray("descriptors");


        if(jdescri!=null)for(int i=0;i<jdescri.length();i++)
        {
            chara.addDescriptor(BuildDescri(jdescri.getJSONObject(i)));

        }
*/

        Log.v("BuildChara","======================"+value);




        return chara;
    }

    public static int parsePropertyNameForChar(String propName)
    {
        if(propName.contentEquals("read"))return BluetoothGattCharacteristic.PROPERTY_READ;
        if(propName.contentEquals("write"))return BluetoothGattCharacteristic.PROPERTY_WRITE;
        if(propName.contentEquals("indicate"))return BluetoothGattCharacteristic.PROPERTY_INDICATE;
        if(propName.contentEquals("notify"))return BluetoothGattCharacteristic.PROPERTY_NOTIFY;

        //TODO more


        return 0;
    }

    public static int PermissionRecommendationNameFromCharProperty(int Property)
    {
        if((Property&
                (BluetoothGattCharacteristic.PROPERTY_READ|BluetoothGattCharacteristic.PROPERTY_INDICATE|BluetoothGattCharacteristic.PROPERTY_NOTIFY)
        )!=0 )
            return BluetoothGattCharacteristic.PERMISSION_READ|BluetoothGattCharacteristic.PERMISSION_WRITE;

        if((Property&
                (BluetoothGattCharacteristic.PROPERTY_WRITE)
        )!=0 )
            return BluetoothGattCharacteristic.PERMISSION_WRITE;

        //TODO more


        return 0;
    }
    public static int parsePermissionNameForChar(String permiName)
    {
        if(permiName.contentEquals("read"))return BluetoothGattCharacteristic.PERMISSION_READ;
        if(permiName.contentEquals("read_MITM"))return BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM;
        if(permiName.contentEquals("read_ENC"))return BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED;
        if(permiName.contentEquals("write"))return BluetoothGattCharacteristic.PERMISSION_WRITE;
        if(permiName.contentEquals("write_MITM"))return BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM;
        if(permiName.contentEquals("write_ENC"))return BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED;

        //TODO more


        return 0;
    }
    public static BluetoothGattDescriptor BuildDescri(JSONObject jDescri)throws JSONException {

        if(jDescri==null)throw new JSONException("null Descriptor");


        BluetoothGattDescriptor descri=null;/*= new BluetoothGattService(
                UUID.fromString(("2800")),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);*/
        //descri=new BluetoothGattDescriptor();




        Log.v("BuildChara","======================");




        return descri;
    }


    public JSONObject ExportJson()
    {
        JSONObject exportJson=new JSONObject();

       // exportJson.put("","");


        return exportJson;
    }
}
