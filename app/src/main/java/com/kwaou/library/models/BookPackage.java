package com.kwaou.library.models;

import java.io.Serializable;
import java.util.ArrayList;

public class BookPackage implements Serializable {

    private String id, userid;
    private ArrayList<Book> bookArrayList;
    private int price;
    private Category category;
    private  int status = 0; // 0 available 1 exchanged 2 sold

    public BookPackage(){

    }


    public BookPackage(String id, String userid, ArrayList<Book> bookArrayList, int price, int status, Category category) {
        this.id = id;
        this.bookArrayList = bookArrayList;
        this.userid = userid;
        this.price = price;
        this.status = status;
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }

    public String getUserid() {
        return userid;
    }

    public String getId() {
        return id;
    }

    public ArrayList<Book> getBookArrayList() {
        if(bookArrayList == null)
            bookArrayList = new ArrayList<>();
        return bookArrayList;
    }

    public int getStatus() {
        return status;
    }

    public int getPrice() {
        return price;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public void setBookArrayList(ArrayList<Book> bookArrayList) {
        this.bookArrayList = bookArrayList;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
