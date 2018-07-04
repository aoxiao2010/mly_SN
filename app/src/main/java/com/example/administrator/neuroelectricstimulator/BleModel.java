package com.example.administrator.neuroelectricstimulator;

import android.bluetooth.BluetoothGatt;

/**
 * Created by Administrator on 2018/5/23.
 */


public class BleModel {
    public static final int BLE_DISCONNET = 0;
    public static final int BLE_CONNETED = 1;
    public static final int BLE_CONNECTING = 2;
    public static final int BLE_SENDING = 3;
    public static final int BLE_SENDED = 4;
    public static final int BLE_DISCOVERING = 5;
    public static final int BLE_DISCOVERIED = 6;
    public static final int BLE_DISCOVERY_FAILED = 7;
    public static final int BLE_SENDED_FAILED = 8;
    public static final int BLE_OPEN_NOTIFING = 9;
    public static final int BLE_OPEN_NOTIFIED = 10;
    public static final int BLE_OPEN_NOTIFY_FAILED = 11;
    public static final int BLE_RECEIVE = 12;
    public static final int BLE_CONNETED_FAILED = 13;
    public static final int BLE_SELECT = 14;
    public static final int BLE_NOTSELECT = 15;

    private String address = null;
    private String name = null;
    private int status = 0;
    private BluetoothGatt gatt = null;
    private String sendData = null;
    private String recvData = null;

    public BleModel(String address, String name) {
        this.address = address;
        this.name = name;
    }

    public BleModel(String address, String name, int status) {
        this.address = address;
        this.name = name;
        this.status = status;
    }

    public String getStatusDescription() {
        switch (status) {
            case BLE_CONNETED:
                return "已连接";
            case BLE_SENDED:
                return "已发送成功";
            case BLE_CONNECTING:
                return "正在连接";
            case BLE_SENDING:
                return "正在发送";
            case BLE_DISCOVERING:
                return "正在扫描服务";
            case BLE_DISCOVERIED:
                return "扫描服务成功";
            case BLE_DISCOVERY_FAILED:
                return "扫描服务失败";
            case BLE_SENDED_FAILED:
                return "发送失败";
            case BLE_OPEN_NOTIFING:
                return "正在打开通知";
            case BLE_OPEN_NOTIFIED:
                return "通知已打开";
            case BLE_OPEN_NOTIFY_FAILED:
                return "通知打开失败";
            case BLE_RECEIVE:
                return "接收数据成功";
            case BLE_CONNETED_FAILED:
                return "连接失败";
            case BLE_SELECT:
                return "已选择";
            case BLE_NOTSELECT:
                return "未选择";
            default:
                return "未连接";
        }
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        if (name == null) {
            return "Unknow Device";
        }
        return name;
    }

    public int getStatus() {
        return status;
    }

    public BluetoothGatt getGatt() {
        return gatt;
    }

    public String getSendData() {
        return sendData;
    }

    public String getRecvData() {
        return recvData;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setGatt(BluetoothGatt gatt) {
        this.gatt = gatt;
    }

    public void setSendData(String sendData) {
        this.sendData = sendData;
    }

    public void setRecvData(String recvData) {
        this.recvData = recvData;
    }
}

