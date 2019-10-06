package com.kwaou.library.models;

public class Complaint {

    private String id, title, desc, reply;
    private User user;

    public Complaint(){

    }

    public Complaint(String id, String title, String desc, String reply, User user) {
        this.id = id;
        this.title = title;
        this.desc = desc;
        this.reply = reply;
        this.user = user;
    }

    public String getReply() {
        return reply;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public User getUser() {
        return user;
    }
}
