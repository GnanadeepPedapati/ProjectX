package com.example.projectx.model;


public class ResponseOverview {

    private String entityName;
    private String newMessageCount;
    private String lastReceivedTime;
    private String lastMessage;

    public ResponseOverview(String entityName, String newMessageCount, String lastReceivedTime, String lastMessage) {
        this.entityName = entityName;
        this.newMessageCount = newMessageCount;
        this.lastReceivedTime = lastReceivedTime;
        this.lastMessage = lastMessage;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getNewMessageCount() {
        return newMessageCount;
    }

    public void setNewMessageCount(String newMessageCount) {
        this.newMessageCount = newMessageCount;
    }

    public String getLastReceivedTime() {
        return lastReceivedTime;
    }

    public void setLastReceivedTime(String lastReceivedTime) {
        this.lastReceivedTime = lastReceivedTime;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
