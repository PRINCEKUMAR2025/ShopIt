package com.example.princeecommerceapp.models;


import com.example.princeecommerceapp.utils.Constants;

public class ChatMessage {
    private String message;
    private int messageType;
    private long timestamp;
    public ChatMessage(String message, int messageType) {
        this.message = message;
        this.messageType = messageType;
        this.timestamp = System.currentTimeMillis();
    }
    public String getMessage() {
        return message;
    }
    public int getMessageType() {
        return messageType;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public boolean isSentByUser() {
        return messageType == Constants.MESSAGE_TYPE_SENT;
    }
}
