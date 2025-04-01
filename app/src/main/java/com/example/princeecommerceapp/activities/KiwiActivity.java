package com.example.princeecommerceapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.princeecommerceapp.R;
import com.example.princeecommerceapp.adapters.ChatAdapter;
import com.example.princeecommerceapp.api.KiwiApiService;
import com.example.princeecommerceapp.api.RetrofitClient;
import com.example.princeecommerceapp.models.ChatMessage;
import com.example.princeecommerceapp.models.ChatRequest;
import com.example.princeecommerceapp.models.ChatResponse;
import com.example.princeecommerceapp.utils.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KiwiActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private EditText messageInput;
    private ImageButton sendButton;
    private KiwiApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kiwi);

        // Initialize API client
        apiService = RetrofitClient.getChatbotApiService();

        // Initialize views
        recyclerView = findViewById(R.id.recycler_chat);
        messageInput = findViewById(R.id.edit_text_message);
        sendButton = findViewById(R.id.button_send);

        // Set up RecyclerView with adapter
        chatAdapter = new ChatAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        // Add welcome message
        addBotMessage("👋 Hi! I'm KIWI. How can I help you today?");

        // Set up send button click listener
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageInput.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendMessage(message);
                    messageInput.setText("");
                }
            }
        });
    }

    private void sendMessage(String message) {
        // Add user message to chat
        addUserMessage(message);

        // Create request
        ChatRequest request = new ChatRequest(message);

        // Make API call
        apiService.sendMessage(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String botResponse = response.body().getResponse();
                    addBotMessage(botResponse);
                } else {
                    addBotMessage("Sorry, I'm having trouble responding right now. Please try again later.");
                }
            }
            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                addBotMessage("Network error. Please check your connection and try again.");
                Toast.makeText(KiwiActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addUserMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, com.example.princeecommerceapp.utils.Constants.MESSAGE_TYPE_SENT);
        chatAdapter.addMessage(chatMessage);
        scrollToBottom();
    }

    private void addBotMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, Constants.MESSAGE_TYPE_RECEIVED);
        chatAdapter.addMessage(chatMessage);
        scrollToBottom();
    }

    private void scrollToBottom() {
        recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
    }
}