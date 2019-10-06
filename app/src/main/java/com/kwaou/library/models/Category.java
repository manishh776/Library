package com.kwaou.library.models;

import java.io.Serializable;

public class Category implements Serializable {
    private String id, name, picUrl;
    int bookCount;

    public Category(){

    }

    public Category(String id, String name, String picUrl, int bookCount) {
        this.id = id;
        this.name = name;
        this.picUrl = picUrl;
        this.bookCount = bookCount;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public int getBookCount() {
        return bookCount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }
}
