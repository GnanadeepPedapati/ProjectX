package com.example.projectx.model;

import java.util.Date;

public class MessageModel {
    public String message;
    public boolean seen;
    public Date messageTime = new Date();
    public String sender;

    // Constructor
    public MessageModel(String message, String sender,boolean seen) {
        this.message = message;
        this.sender = sender;
        this.seen = seen;
    }
    public MessageModel(){

    }


}
