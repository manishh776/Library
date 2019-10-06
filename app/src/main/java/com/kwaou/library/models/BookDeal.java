package com.kwaou.library.models;

public class BookDeal {

    private String id;
    private boolean forSale;
    private BookPackage old, newbook;
    private User lender, receiver;
    private int status;// 1-requested 2-accepted 0-rejected
    private String date;

    public BookDeal(){

    }

    public BookDeal(String id, BookPackage old, BookPackage newbook, User lender, User receiver, boolean forSale, int status, String date) {
        this.id = id;
        this.old = old;
        this.newbook = newbook;
        this.lender = lender;
        this.receiver = receiver;
        this.forSale = forSale;
        this.status = status;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public int getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }

    public boolean isForSale() {
        return forSale;
    }

    public BookPackage getOld() {
        return old;
    }

    public BookPackage getNewbook() {
        return newbook;
    }

    public User getLender() {
        return lender;
    }

    public User getReceiver() {
        return receiver;
    }
}
