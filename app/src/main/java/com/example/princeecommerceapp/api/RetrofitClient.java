package com.example.princeecommerceapp.api;

import com.example.princeecommerceapp.utils.Constants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static KiwiApiService chatbotApiService = null;
    public static synchronized KiwiApiService getChatbotApiService() {
        if (chatbotApiService == null) {
            if (retrofit == null) {
                retrofit = new Retrofit.Builder()
                        .baseUrl(Constants.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            chatbotApiService = retrofit.create(KiwiApiService.class);
        }
        return chatbotApiService;
    }
}
