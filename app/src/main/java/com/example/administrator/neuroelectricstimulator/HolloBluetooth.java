package com.example.administrator.neuroelectricstimulator;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.UUID;

/**
 * Created by Administrator on 2018/5/23.
 */

public class HolloBluetooth extends BluetoothLe
{
    private final static String TAG = HolloBluetooth.class.getSimpleName();

    public final static int HOLLO_BLE_CONNECTED = 1;			//蓝牙已连接
    public final static int HOLLO_BLE_SERVICE_DISCOVERY = 2;	//蓝牙已发现
    public final static int HOLLO_BLE_DISCONNECTED = 3;			//蓝牙断开

    public final static int RECV_TIME_OUT_SHORT	= 2000;		//短的接收超时，ms
    public final static int RECV_TIME_OUT_MIDDLE = 5000;	//中长的接收超时，ms
    public final static int RECV_TIME_OUT_LONG	= 10000;	//长的接收超时，ms

    public final static int BLE_SEND_DATA_LEN_MAX = 20;

    public final static UUID UUID_HOLLO_SERVICE = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");

    public final static UUID UUID_HOLLO_DATA_RECEIVE = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_HOLLO_DATA_SEND = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");

    private WaitEvent connectEvent = new WaitEvent();

    private WaitEvent stateEvent = new WaitEvent();

    private WaitEvent sendEvent = new WaitEvent();

    private int mBleState = HOLLO_BLE_DISCONNECTED;

    private static HolloBluetooth mHolloBluetooth = null;

    private OnHolloBluetoothCallBack mBleCallBack = null;
    private OnHolloBluetoothCallBack1 mBleCallBack1 = null;

    public interface OnHolloBluetoothCallBack
    {
        public void OnHolloBluetoothState(int state);
        public void OnReceiveData(byte[] recvData);
    }
    public interface OnHolloBluetoothCallBack1
    {
        public void OnHolloBluetoothState(int state);
        public void OnReceiveData(byte[] recvData);
    }

    private HolloBluetooth(Context context)
    {
        super(context);
    }

    /**
     * 获取HolloBluetooth类实例
     *
     * @param activity 界面的activity
     *
     * @return HolloBluetooth类实例，当HolloBluetooth未实例化过，且activity为null时，返回null
     * 		       当HolloBluetooth已实例化过，无论activity是否为null，皆返回HolloBluetooth类的实例
     *
     */
    public static synchronized HolloBluetooth getInstance(Context context)
    {
        if (mHolloBluetooth == null)
        {
            if(context == null)
                return null;

            mHolloBluetooth = new HolloBluetooth(context);
        }
        return mHolloBluetooth;
    }

    /**
     * 连接远端蓝牙设备
     *
     * @param address 远端蓝牙mac地址
     * @param bleCallBack 蓝牙回调函数，当蓝牙非主动断开时，调用
     *
     * @return 连接成功返回true，否则返回false
     *
     */
    public boolean connectDevice(String address, OnHolloBluetoothCallBack bleCallBack)
    {

        mBleCallBack = bleCallBack;

        connectEvent.Init();

//		if(mBleState == HOLLO_BLE_SERVICE_DISCOVERY)
//			return true;

        if(!super.connectDevice(address, mGattCallback,0))
            return false;

        if (connAddress(address,0)==false){
            return false;
        }

        return true;
    }
    public boolean connectDevice1(String address, OnHolloBluetoothCallBack1 bleCallBack){
        mBleCallBack1 = bleCallBack;
        connectEvent.Init();
        if(!super.connectDevice(address, mGattCallback1,1))
            return false;
        if (connAddress(address,1)==false){
            return false;
        }

        return true;
    }

    public boolean connAddress(String address,Integer vl){
        if(WaitEvent.SUCCESS != connectEvent.waitSignal(RECV_TIME_OUT_MIDDLE))
        {
            int position=0;
            for(int i =0;i<mBluetoothDeviceAddress.size();i++){
                if(mBluetoothDeviceAddress.get(i)==address){
                    position=i;
                }
            }
            BluetoothGatt gatt=connectionQueue.get(position);
            disconnectDevice1(gatt,vl);
            return false;
        }
        return true;
    }



    public void disconnectDevice(BluetoothGatt gatt)
    {
        mBleState = HOLLO_BLE_DISCONNECTED;
        mBleCallBack = null;
        mBleCallBack1=null;
        super.disconnectDevice(gatt);
    }
    public void disconnectDevice1(BluetoothGatt gatt,Integer vl)
    {
        mBleState = HOLLO_BLE_DISCONNECTED;
        if (vl==0){
            mBleCallBack = null;
        }else if (vl==1){
            mBleCallBack1=null;
        }
        super.disconnectDevice(gatt);
    }

    /**
     * 是否已连接
     *
     * @return 已连接返回true，否则返回false
     *
     */
    public boolean isConnect()
    {
        return (mBleState == HOLLO_BLE_SERVICE_DISCOVERY);
    }


    /**
     * 唤醒蓝牙,当有错误时，会抛出HolloBluetoothException异常
     *
     * @param 无
     *
     * @return 手环已佩戴时，返回true，否则返回false
     *
     */
    public boolean wakeUpBle(String address)
    {
        int position=0;
        for(int i =0;i<mBluetoothDeviceAddress.size();i++){
            if(mBluetoothDeviceAddress.get(i)==address){
                position=i;
            }
        }
        BluetoothGatt gatt=connectionQueue.get(position);
        if(gatt == null)
            return false;

        BluetoothGattCharacteristic character;

        character = gatt.getService(UUID_HOLLO_SERVICE).getCharacteristic(UUID_HOLLO_DATA_RECEIVE);

        stateEvent.Init();
        if(!setCharacteristicNotification(character,gatt, true))
            return false;

        if(WaitEvent.SUCCESS != stateEvent.waitSignal(RECV_TIME_OUT_MIDDLE))
            return false;

        return true;
    }

    public boolean sendData(byte[] data,String address)
    {
        int position=0;
        for(int i =0;i<mBluetoothDeviceAddress.size();i++){
            if(mBluetoothDeviceAddress.get(i)==address){
                position=i;
            }
        }
        BluetoothGatt gatt=connectionQueue.get(position);
        if(gatt== null)
            return false;

        BluetoothGattCharacteristic character;

        character = gatt.getService(UUID_HOLLO_SERVICE).getCharacteristic(UUID_HOLLO_DATA_SEND);

        int nCount = data.length/BLE_SEND_DATA_LEN_MAX;
        if(data.length%BLE_SEND_DATA_LEN_MAX != 0)
            nCount++;

        byte[] temp;
        for (int i = 0; i < nCount; i++)
        {
            sendEvent.Init();

            if( (i+1) != nCount)
            {
                temp = new byte[BLE_SEND_DATA_LEN_MAX];
            }
            else
            {
                temp = new byte[data.length-BLE_SEND_DATA_LEN_MAX*i];
            }

            for (int j = 0; j < temp.length; j++)
            {
                temp[j] = data[i*(BLE_SEND_DATA_LEN_MAX)+j];
            }

            character.setValue(temp);
            //character.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            if(!gatt.writeCharacteristic(character))
                return false;

            if(WaitEvent.SUCCESS != sendEvent.waitSignal(RECV_TIME_OUT_MIDDLE))
                return false;
        }

        return true;
    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState)
        {
            // String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                mBleState = HOLLO_BLE_CONNECTED;
                gatt.discoverServices();
                Log.d(TAG, "连接成功 " + status);
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                mBleState = HOLLO_BLE_DISCONNECTED;
                if(mBleCallBack != null)
                    mBleCallBack.OnHolloBluetoothState(mBleState);

                Log.d(TAG, "Disconnected from GATT server "+mBleState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            if(status == BluetoothGatt.GATT_SUCCESS)
                mBleState = HOLLO_BLE_SERVICE_DISCOVERY;
            connectEvent.setSignal(status == BluetoothGatt.GATT_SUCCESS);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                // broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                // For all other profiles, writes the data formatted in HEX.
                final byte[] data = characteristic.getValue();
                if (data != null && data.length > 0)
                {
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status)
        {
            if (characteristic.getUuid().equals(UUID_HOLLO_DATA_SEND))
            {
                sendEvent.setSignal(status == BluetoothGatt.GATT_SUCCESS);
            }
            else
            {
                sendEvent.setSignal(false);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic)
        {
            // broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0)
            {
                if(mBleCallBack != null)
                    mBleCallBack.OnReceiveData(data);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                Log.d(TAG, "Descript success ");
                if(ConvertData.cmpBytes(descriptor.getValue(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE))
                {
                    stateEvent.setSignal(true);
                }
            }
            else
            {
                stateEvent.setSignal(false);
            }
        }
    };


    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback1 = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState)
        {
            // String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                mBleState = HOLLO_BLE_CONNECTED;
                gatt.discoverServices();
                Log.d(TAG, "连接成功 " + status);
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                mBleState = HOLLO_BLE_DISCONNECTED;
                if(mBleCallBack1 != null)
                    mBleCallBack1.OnHolloBluetoothState(mBleState);

                Log.d(TAG, "Disconnected from GATT server "+mBleState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            if(status == BluetoothGatt.GATT_SUCCESS)
                mBleState = HOLLO_BLE_SERVICE_DISCOVERY;
            connectEvent.setSignal(status == BluetoothGatt.GATT_SUCCESS);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                // broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                // For all other profiles, writes the data formatted in HEX.
                final byte[] data = characteristic.getValue();
                if (data != null && data.length > 0)
                {
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status)
        {
            if (characteristic.getUuid().equals(UUID_HOLLO_DATA_SEND))
            {
                sendEvent.setSignal(status == BluetoothGatt.GATT_SUCCESS);
            }
            else
            {
                sendEvent.setSignal(false);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic)
        {
            // broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0)
            {
                if(mBleCallBack1 != null)
                    mBleCallBack1.OnReceiveData(data);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                Log.d(TAG, "Descript success ");
                if(ConvertData.cmpBytes(descriptor.getValue(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE))
                {
                    stateEvent.setSignal(true);
                }
            }
            else
            {
                stateEvent.setSignal(false);
            }
        }
    };


    private class WaitEvent
    {
        public final static int ERROR_OTHER = 2;
        public final static int ERROR_TIME_OUT = 1;
        public final static int SUCCESS = 0;

        private Object mSignal;
        private boolean mFlag;
        private int mResult;

        private MyThread myThread;

        public WaitEvent()
        {
            mSignal = new Object();
            mFlag = true;
            mResult = SUCCESS;
        }
        public void Init()
        {
            mFlag = true;
            mResult = SUCCESS;
            Log.d(TAG, "Init Event");
        }
        public int waitSignal(int millis)
        {
            myThread = new MyThread();
            myThread.startThread(millis);
            if(!mFlag)
                return mResult;

            synchronized (mSignal)
            {
                try
                {
                    Log.d(TAG, "waitSignal ");
                    mSignal.wait();
                    Log.d(TAG, "waitSignal over");
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            return mResult;
        }

        public void setSignal(boolean bSuccess)
        {
            synchronized (mSignal)
            {
                Log.d(TAG, "setSignal");
                mFlag = false;
                if(!bSuccess)
                    mResult = ERROR_OTHER;
                if(myThread != null)
                    myThread.stopThread();
                mSignal.notify();
            }
        }

        private void waitTimeOut()
        {
            Log.d(TAG, "waitTimeOut");
            mResult = ERROR_TIME_OUT;
            setSignal(true);
        }

        class MyThread extends Thread
        {
            boolean mThreadAlive = false;
            int mCount = 0;
            int mTotal = 0;
            public void startThread(int millis)
            {
                mTotal = millis/10;
                mCount = 0;
                mThreadAlive = true;
                start();
                Log.d(TAG, "runable start");
            }

            public void stopThread()
            {
                Log.d(TAG, "runable stop");
                mThreadAlive = false;
            }

            @Override
            public void run()
            {
                while(true)
                {
                    //Log.d(TAG, "Thread Running");
                    try
                    {
                        mCount++;
                        Thread.sleep(10);

                        if(!mThreadAlive)
                        {
                            return ;
                        }

                        if(mCount > mTotal)		//超时
                        {
                            waitTimeOut();
                            return ;
                        }
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

}
