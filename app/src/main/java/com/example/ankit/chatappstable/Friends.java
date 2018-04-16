package com.example.ankit.chatappstable;

/**
 * Created by Ankit on 05-Mar-18.
 */

public class Friends {

    private String date;

    private Friends(){

    }
    public Friends(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
