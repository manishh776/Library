package com.kwaou.library.models;

import java.io.Serializable;

public class Book implements Serializable {

    private String id, url, userid, title, desc, picUrl;
    private Category category;
    private int price;
    private  int status = 0; // 0 available 1 exchanged 2 sold

    public Book(){

    }

    public Book(String id, String url, String userid, String title, String desc, String picUrl, Category category, int price) {
        this.id = id;
        this.url = url;
        this.userid = userid;
        this.title = title;
        this.desc = desc;
        this.picUrl = picUrl;
        this.category = category;
        this.price = price;
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getUserid() {
        return userid;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public Category getCategory() {
        return category;
    }

    public int getPrice() {
        return price;
    }

}
