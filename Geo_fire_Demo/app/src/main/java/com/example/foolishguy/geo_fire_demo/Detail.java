package com.example.foolishguy.geo_fire_demo;

/**
 * Created by Foolish Guy on 1/3/2017.
 */

public class Detail {
    String name, phone;

    public Detail (String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
