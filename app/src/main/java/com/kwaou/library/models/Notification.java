package com.kwaou.library.models;

public class Notification {

    private String id, title, message, time, userid;
    User from;

    public Notification(){

    }

    public Notification(String id, String title, String message, String time, String userid, User from) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.userid = userid;
        this.time = time;
        this.from = from;
    }

    public String getUserid() {
        return userid;
    }

    public User getFrom() {
        return from;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }
}
