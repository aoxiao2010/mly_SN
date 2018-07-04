package com.example.administrator.neuroelectricstimulator;

import android.content.Intent;

/**
 * Created by Administrator on 2018/6/8.
 */

public class EquipmentHolder {


    private int ID = 0;
    private int CFID = 0; //处方ID
    private String Pres = null;//处方部位
    private String Name = null;//处方范围
    private String Address = null;//MAC地址
    private String Capacity = null;//"容量"
    private String ProdCode = null;//"产品编号"
    private Boolean Conn=false;//连接状态
    public EquipmentHolder(){}
    public EquipmentHolder(int id, String name,String address, String pres, String capacity, String prodCode,int cfId,boolean conn) {
        this.ID = id;
        this.Address = address;
        this.Name = name;
        this.CFID=cfId;
        this.Pres = pres;
        this.Capacity = capacity;
        this.ProdCode = prodCode;
        this.Conn=conn;
    }

    public Boolean getConn() {
        return Conn;
    }

    public void setConn(Boolean conn) {
        Conn = conn;
    }

    public int getCFID() {
        return CFID;
    }

    public void setCFID(int CFID) {
        this.CFID = CFID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getPres() {
        return Pres;
    }

    public void setPres(String pres) {
        Pres = pres;
    }

    public String getCapacity() {
        return Capacity;
    }

    public void setCapacity(String capacity) {
        Capacity = capacity;
    }

    public String getProdCode() {
        return ProdCode;
    }

    public void setProdCode(String prodCode) {
        ProdCode = prodCode;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }
}
