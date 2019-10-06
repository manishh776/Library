package com.kwaou.library.models;


public class User {

    private String id, name, email, phone, dob, password;
    private String picUrl, token;

    public User(){

    }

    public User(String id, String name, String email, String phone, String dob, String password, String picUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.dob = dob;
        this.password = password;
        this.picUrl = picUrl;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getDob() {
        return dob;
    }

    public String getPassword() {
        return password;
    }

    public String getPicUrl() {
        return picUrl;
    }
}
