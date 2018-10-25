package com.csim.scu.aibox.model;

/**
 * Created by kenny on 2018/10/24.
 */

public class WowLocation {

    private String type;
    private String name;
    private String addr;
    private String phone;

    public WowLocation(String type, String name, String addr, String phone) {
        this.type = type;
        this.name = name;
        this.addr = addr;
        this.phone = phone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
