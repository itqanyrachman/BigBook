package com.pietro.gadgetlog.model;

import java.io.Serializable;

public class Penerbit implements Serializable {
    private String id, name, country, img;

    public Penerbit(String name, String country, String img) {
        this.name = name;
        this.country = country;
        this.img = img;
    }

    public Penerbit(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
