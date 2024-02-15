package com.pupfinds.app;

public class MessageModel {

    private String messageID;
    private String senderID;
    private String receiverID;
    private String message;

    private long timestamp;

    public MessageModel(String messageID, String senderID, String receiverID, String message, long timestamp) {
        this.messageID = messageID;
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.message = message;
        this.timestamp = timestamp;
    }

    public MessageModel() {

    }

    public String getMessageID() {
        return messageID;
    }

    public String getSenderID() {
        return senderID;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
