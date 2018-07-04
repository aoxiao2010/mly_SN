package com.example.administrator.neuroelectricstimulator;

/**
 * Created by Administrator on 2018/6/13.
 */

public class PrescriptionHolder {
    private int ID = 100;
    private String Name = null;//FD 处方
    private String Acpoint = null;//足三里 部位名称
    private String Freq = null; //频率
    private String PulseWidth = null;//脉冲宽度
    private String OnTime = null; //刺激时间
    private String OffTime = null;//休息时间
    private String Strength = null; //强度
    private String PulseDirection = null; //脉冲方向
    private String PointCode = null; //穴位编码

    public PrescriptionHolder(){}
    public PrescriptionHolder(int id, String name,String acpoint, String freq, String pulseWidth, String onTime, String offTime, String strength, String pulseDirection, String pointCode) {
        this.ID = id;
        this.Name = name;
        this.Acpoint = acpoint;
        this.Freq = freq;
        this.PulseWidth = pulseWidth;
        this.OnTime = onTime;
        this.OffTime = offTime;
        this.Strength = strength;
        this.PulseDirection = pulseDirection;
        this.PointCode = pointCode;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getAcpoint() {
        return Acpoint;
    }

    public void setAcpoint(String acpoint) {
        Acpoint = acpoint;
    }

    public String getFreq() {
        return Freq;
    }

    public void setFreq(String freq) {
        Freq = freq;
    }

    public String getPulseWidth() {
        return PulseWidth;
    }

    public void setPulseWidth(String pulseWidth) {
        PulseWidth = pulseWidth;
    }

    public String getOnTime() {
        return OnTime;
    }

    public void setOnTime(String onTime) {
        OnTime = onTime;
    }

    public String getOffTime() {
        return OffTime;
    }

    public void setOffTime(String offTime) {
        OffTime = offTime;
    }

    public String getStrength() {
        return Strength;
    }

    public void setStrength(String strength) {
        Strength = strength;
    }

    public String getPulseDirection() {
        return PulseDirection;
    }

    public void setPulseDirection(String pulseDirection) {
        PulseDirection = pulseDirection;
    }

    public String getPointCode() {
        return PointCode;
    }

    public void setPointCode(String pointCode) {
        PointCode = pointCode;
    }


}
