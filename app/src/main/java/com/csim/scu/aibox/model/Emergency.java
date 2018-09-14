package com.csim.scu.aibox.model;

/**
 * Created by kenny on 2018/9/3.
 */

public class Emergency {

    private String name;
    private String phone;

    public Emergency(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
