package com.example.administrator.neuroelectricstimulator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 2018/5/23.
 */

public class BluetoothLe
{
    private final static String TAG = BluetoothLe.class.getSimpleName();

    private Context mContext;

    protected BluetoothManager mBluetoothManager;
    public BluetoothAdapter mBluetoothAdapter;
    protected ArrayList<String> mBluetoothDeviceAddress= new ArrayList<String>();
    //protected BluetoothGatt mBluetoothGatt;
    protected ArrayList<BluetoothGatt> connectionQueue = new ArrayList<BluetoothGatt>();

    private BluetoothAdapter.LeScanCallback mScanCallback;

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_CLIENT_CHARACTERISTIC_CONFIG =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public BluetoothLe(Context context)
    {
        mContext = context;
    }

//    public BluetoothGatt getGatt()
//    {
//        return mBluetoothGatt;
//    }

    /**
     * 判断本地设备是否支持蓝牙ble
     *
     * @param 无
     *
     * @return 支持返回true，否则返回false
     *
     */
    public boolean isBleSupported()
    {
        if(!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
            return false;

        return true;
    }

    /**
     * 判断本地蓝牙是否打开
     *
     * @param 无
     *
     * @return 已打开返回true，否则返回false
     *
     */
    public boolean isOpened()
    {
        if(!mBluetoothAdapter.isEnabled())
            return false;

        return true;
    }

    /**
     * 设置扫描回调函数
     *
     * @param scanCallback 扫描回调函数
     *
     * @return 设置成功返回true，否则返回false
     *
     */
    public boolean setScanCallBack(BluetoothAdapter.LeScanCallback scanCallback)
    {
        if(scanCallback == null)
            return false;

        mScanCallback = scanCallback;
        return true;
    }

    /**
     * 开始扫描
     *
     * @param 无
     *
     * @return 开始扫描成功返回true，否则返回false
     *
     */
    public boolean startLeScan()
    {
        if(mScanCallback == null)
            return false;

        return mBluetoothAdapter.startLeScan(mScanCallback);
    }

    /**
     * 停止扫描
     *
     * @param 无
     *
     * @return 停止扫描成功返回true，否则返回false
     *
     */
    public boolean stopLeScan()
    {
        if(mScanCallback == null)
            return false;

        mBluetoothAdapter.stopLeScan(mScanCallback);

        return true;
    }

    /**
     * 连接本地蓝牙设备
     *
     * @param 无
     *
     * @return 连接成功返回true，否则返回false
     *
     */
    public boolean connectLocalDevice()
    {
        if (mBluetoothManager == null)
        {
            mBluetoothManager = (BluetoothManager)mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null)
            {
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null)
        {
            return false;
        }

        return true;
    }

    /**
     * 断开本地蓝牙设备
     *
     * @param 无
     *
     * @return 断开成功返回true，否则返回false
     *
     */
    public void disconnectLocalDevice(BluetoothGatt gatt)
    {
//        if (mBluetoothGatt == null)
//        {
//            return;
//        }
//        mBluetoothGatt.close();
//        mBluetoothGatt = null;
        if (!connectionQueue.isEmpty()){
            if (gatt != null){
                for(final BluetoothGatt bluetoothGatt:connectionQueue){
                    if(bluetoothGatt.equals(gatt)){
                        bluetoothGatt.close();
                    }
                }
            }
        }

    }

    /**
     * 连接远端蓝牙设备
     *
     * @param address 远端蓝牙mac地址
     * @param gattCallback 蓝牙数据的回调函数
     *
     * @return 连接成功返回true，否则返回false
     *
     */
    public boolean connectDevice(final String address, BluetoothGattCallback gattCallback,Integer vl)
    {
        if (mBluetoothAdapter == null || address == null)
        {
            return false;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null)
        {
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        BluetoothGatt bluetoothGatt;
        bluetoothGatt = device.connectGatt(mContext, false, gattCallback);
        if(bluetoothGatt==null){
            return false;
        }

        if(checkGatt(bluetoothGatt)){
            if (vl==0){
                if (connectionQueue.size()>0 && mBluetoothDeviceAddress.size()>0){
                    if (address!=mBluetoothDeviceAddress.get(0)){
                         String address1=mBluetoothDeviceAddress.get(0);
                         BluetoothGatt bGatt1=connectionQueue.get(0);
                         connectionQueue.set(0,bluetoothGatt);
                         mBluetoothDeviceAddress.set(0,address);
                         connectionQueue.set(1,bGatt1);
                         mBluetoothDeviceAddress.set(1,address1);
                    }else{
                         connectionQueue.set(0,bluetoothGatt);
                         mBluetoothDeviceAddress.set(0,address);
                    }
                }else{
                    connectionQueue.add(bluetoothGatt);
                    mBluetoothDeviceAddress.add(address);
                }
            }
            if (vl==1){
                if (connectionQueue.size()>1 && mBluetoothDeviceAddress.size()>1){
                    connectionQueue.remove(1);
                    mBluetoothDeviceAddress.remove(1);
                    if (address==mBluetoothDeviceAddress.get(0)){
                        connectionQueue.set(0,bluetoothGatt);
                        mBluetoothDeviceAddress.set(0,address);
                    }
                }
                if (connectionQueue.size()==0 && mBluetoothDeviceAddress.size()==0){
                    connectionQueue.add(bluetoothGatt);
                    mBluetoothDeviceAddress.add(address);
                }
                connectionQueue.add(bluetoothGatt);
                mBluetoothDeviceAddress.add(address);
            }
        }else{
            return false;
        }
        return true;
    }
    private boolean checkGatt(BluetoothGatt bluetoothGatt) {
        if (!connectionQueue.isEmpty()) {
            for(BluetoothGatt btg:connectionQueue){
                if(btg.equals(bluetoothGatt)){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 断开远端蓝牙设备
     *
     * @param 无
     *
     * @return 成功
     *
     */
    public void disconnectDevice(BluetoothGatt gatt)
    {
        if (mBluetoothAdapter == null || connectionQueue.isEmpty())
        {
            return ;
        }
        if (gatt != null){
            for(final BluetoothGatt bluetoothGatt:connectionQueue){
                if(bluetoothGatt.equals(gatt)){
                    bluetoothGatt.disconnect();
                }
            }
        }
        Log.d(TAG, "Bluetooth disconnect");
    }

    /**
     * 读取服务数据，数据在{@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}中
     * 该操作为异步的
     *
     * @param characteristic 服务特征值
     *
     * @return 读取请求成功返回true，否则返回false
     *
     */
    public boolean readCharacteristic(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt)
    {
        if (mBluetoothAdapter == null || gatt == null)
        {
            return false;
        }
        return gatt.readCharacteristic(characteristic);
    }

    /**
     * 写服务数据，数据在{@code BluetoothGattCallback#onCharacteristicWrite(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}中
     * 该操作为异步的
     *
     * @param characteristic 服务特征值
     *
     * @return 写请求成功返回true，否则返回false
     *
     */
    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic,BluetoothGatt gatt)
    {
        if (mBluetoothAdapter == null || gatt == null)
        {
            return false;
        }
        //characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        return gatt.writeCharacteristic(characteristic);
    }

    /**
     * 设置服务通知，数据在{@code BluetoothGattCallback#onDescriptorWrite(BluetoothGatt gatt,BluetoothGattDescriptor descriptor, int status)}中
     * 该操作为异步的
     *
     * @param characteristic 服务特征值
     * @param enabled 当为true表示打开通知，否则为关闭通知
     *
     * @return 设置通知请求成功返回true，否则返回false
     *
     */
    public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic,BluetoothGatt gatt,boolean enabled)
    {
        if (mBluetoothAdapter == null || gatt == null)
        {
            return false;
        }
        if(!gatt.setCharacteristicNotification(characteristic, enabled))
            return false;

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_CLIENT_CHARACTERISTIC_CONFIG);
        if(descriptor == null)
            return false;

        byte[] data;
        if(enabled)
            data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
        else
            data = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;

        if(!descriptor.setValue(data))
            return false;
        return gatt.writeDescriptor(descriptor);
    }

    /**
     * 获取服务列表
     *
     * @param 无
     *
     * @return 服务列表
     *
     */
    public List<BluetoothGattService> getSupportedGattServices(BluetoothGatt gatt)
    {
        if (gatt == null)
            return null;

        return gatt.getServices();
    }
}
