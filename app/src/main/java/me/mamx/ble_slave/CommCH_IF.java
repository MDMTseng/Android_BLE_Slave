package me.mamx.ble_slave;

/**
 * Created by ctseng on 9/18/15.
 */
public abstract class CommCH_IF {


    abstract boolean RecvData(Object CH, byte[] data);


    boolean SetData(Object CH,byte[] data){return false;}


    boolean GetData(Object CH,byte[] data){return false;}


    abstract boolean SendData(Object CH,byte[] data);

}
