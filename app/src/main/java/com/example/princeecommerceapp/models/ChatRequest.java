package com.example.princeecommerceapp.models;

public class ChatRequest {
    private String message;
    private String user_id;
    public ChatRequest(String message) {
        this.message = message;
    }
    public ChatRequest(String message, String user_id) {
        this.message = message;
        this.user_id = user_id;
    }
    public String getMessage() {
        return message;
    }
    public String getUserId() {
        return user_id;
    }
}
