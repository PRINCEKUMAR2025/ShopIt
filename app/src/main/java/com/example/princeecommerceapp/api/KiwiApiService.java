package com.example.princeecommerceapp.api;

import com.example.princeecommerceapp.models.ChatRequest;
import com.example.princeecommerceapp.models.ChatResponse;

import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface KiwiApiService {
    @POST("/api/chat")
    Call<ChatResponse> sendMessage(@Body ChatRequest request);
}
