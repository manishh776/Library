package com.kwaou.library.models;

public class Admin {

    private String username, password, token;
    private int N;

    public Admin(){

    }

    public Admin(String username, String password, String token) {
        this.username = username;
        this.password = password;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getN() {
        return N;
    }

    public void setN(int N) {
        this.N = N;
    }
}
