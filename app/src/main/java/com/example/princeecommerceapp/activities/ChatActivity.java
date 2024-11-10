package com.example.princeecommerceapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.princeecommerceapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String chatId = "chat1"; // Chat ID
    private String userId; // User ID

    private LinearLayout chatLayout;
    private EditText messageInput;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Firestore and Auth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get the current user's ID after initializing auth
        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getEmail(); // User ID
        } else {
            // Handle the case where the user is not logged in
            // You might want to show a message or redirect to login
            finish(); // Close the activity if user is not logged in
            return;
        }

        // Initialize UI components
        chatLayout = findViewById(R.id.chatLayout);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // Set up the send button click listener
        sendButton.setOnClickListener(v -> sendMessage());

        // Load messages from Firestore
        loadMessages();
    }

    private void loadMessages() {
        CollectionReference messagesRef = db.collection("chats").document(chatId).collection("messages");
        messagesRef.orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                return;
            }
            for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                if (dc.getType() == DocumentChange.Type.ADDED) {
                    String senderId = dc.getDocument().getString("senderId");
                    String text = dc.getDocument().getString("text");
                    addMessageToChat(senderId, text);
                }
            }
        });
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            Map<String, Object> message = new HashMap<>();
            message.put("senderId", userId);
            message.put("text", messageText);
            message.put("timestamp", System.currentTimeMillis());

            db.collection("chats").document(chatId).collection("messages").add(message);
            messageInput.setText(""); // Clear the input field after sending
        }
    }

    private void addMessageToChat(String senderId, String text) {
        TextView messageView = new TextView(this);
        messageView.setText(senderId + ": " + text);
        messageView.setTextSize(16); // Set text size
        messageView.setPadding(10, 10, 10, 10); // Add padding
        messageView.setBackgroundResource(R.drawable.message_background); // Set background for messages
        messageView.setTextColor(senderId.equals(userId) ? getResources().getColor(R.color.black) : getResources().getColor(R.color.red)); // Different colors for sender and receiver
        messageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        chatLayout.addView(messageView);
    }
}