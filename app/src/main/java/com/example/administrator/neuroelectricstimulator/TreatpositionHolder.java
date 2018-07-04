package com.example.administrator.neuroelectricstimulator;

/**
 * Created by Administrator on 2018/6/13.
 */

public class TreatpositionHolder {
    private int ID = 1;
    private int EquipmentID = 0;//设备ID
    private String SiteName = null;//部位名称
    private String PresName = null;//适用范围
    private String Address = null;//MAC地址

    public TreatpositionHolder(){}
    public TreatpositionHolder(int id, int equipmentID,String siteName, String presName, String address) {
        this.ID = id;
        this.EquipmentID = equipmentID;
        this.SiteName = siteName;
        this.PresName = presName;
        this.Address = address;

    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getEquipmentID() {
        return EquipmentID;
    }

    public void setEquipmentID(int equipmentID) {
        EquipmentID = equipmentID;
    }

    public String getSiteName() {
        return SiteName;
    }

    public void setSiteName(String siteName) {
        SiteName = siteName;
    }

    public String getPresName() {
        return PresName;
    }

    public void setPresName(String presName) {
        PresName = presName;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }
}
