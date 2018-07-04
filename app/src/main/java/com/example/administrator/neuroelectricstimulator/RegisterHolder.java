package com.example.administrator.neuroelectricstimulator;

/**
 * Created by Administrator on 2018/6/14.
 */

public class RegisterHolder {
    private int ID = 0;
    private String Name = null;
    private String XName = null;
    private String HospId = null;
    private String Phone = null;
    private String Password = null;
    private Boolean Forget = false;
    public RegisterHolder(){}
    public RegisterHolder(int id, String name,String xname, String hospId, String phone, String password,Boolean forget) {
        this.ID = id;
        this.Name = name;
        this.XName = xname;
        this.HospId = hospId;
        this.Phone = phone;
        this.Password = password;
        this.Forget=forget;
    }

    public Boolean getForget() {
        return Forget;
    }

    public void setForget(Boolean forget) {
        Forget = forget;
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

    public String getXName() {
        return XName;
    }

    public void setXName(String XName) {
        this.XName = XName;
    }

    public String getHospId() {
        return HospId;
    }

    public void setHospId(String hospId) {
        HospId = hospId;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}
