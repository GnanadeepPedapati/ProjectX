package com.example.projectx.model;

import java.util.Date;

public class MessageModel {
    public String message;
    public int messageType;
    public Date messageTime = new Date();

    // Constructor
    public MessageModel(String message, int messageType) {
        this.message = message;
        this.messageType = messageType;
    }
    public MessageModel(){

    }

    public String getMessageText() {
        return message;
    }

    public void setMessageText(String messageText) {
        this.message = messageText;
    }


}
