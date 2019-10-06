package com.kwaou.library.models;

import java.io.Serializable;
import java.util.ArrayList;

public class BookPackage implements Serializable {

    private String id, userid;
    private ArrayList<Book> bookArrayList;
    private int price;
    private  int status = 0; // 0 available 1 exchanged 2 sold

    public BookPackage(){

    }


    public BookPackage(String id, String userid, ArrayList<Book> bookArrayList, int price, int status) {
        this.id = id;
        this.bookArrayList = bookArrayList;
        this.userid = userid;
        this.price = price;
        this.status = status;
    }

    public String getUserid() {
        return userid;
    }

    public String getId() {
        return id;
    }

    public ArrayList<Book> getBookArrayList() {
        return bookArrayList;
    }

    public int getStatus() {
        return status;
    }

    public int getPrice() {
        return price;
    }
}
