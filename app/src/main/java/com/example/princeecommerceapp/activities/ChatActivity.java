package com.example.princeecommerceapp.activities;

import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    String chatId; // Chat ID
    private String userId; // User ID

    private LinearLayout chatLayout;
    private EditText messageInput;
    private Button sendButton;
    private Button closeButton; // Add close button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Firestore and Auth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        chatId= auth.getCurrentUser().getEmail().toString();

        // Get the current user's ID after initializing auth
        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getEmail(); // User ID
        } else {
            // Handle the case where the user is not logged in
            finish(); // Close the activity if user is not logged in
            return;
        }

        // Initialize UI components
        chatLayout = findViewById(R.id.chatLayout);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        closeButton = findViewById(R.id.closeButton); // Initialize close button

        // Set up the send button click listener
        sendButton.setOnClickListener(v -> sendMessage());

        // Set up the close button click listener
        closeButton.setOnClickListener(v -> closeChat());

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

            // Create the parent document if it doesn't exist
            db.collection("chats").document(chatId)
                    .set(new HashMap<>(), SetOptions.merge()) // Create or merge the parent document
                    .addOnSuccessListener(aVoid -> {
                        // Add the message to the messages subcollection
                        db.collection("chats").document(chatId).collection("messages")
                                .add(message)
                                .addOnSuccessListener(documentReference -> {
                                    messageInput.setText(""); // Clear the input field after sending
                                })
                                .addOnFailureListener(e -> Log.e("ChatActivity", "Error sending message", e));
                    })
                    .addOnFailureListener(e -> Log.e("ChatActivity", "Error creating parent document", e));
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

    private void closeChat() {
        // Optionally delete chat history from Firestore
        deleteChatHistory();
        finish(); // Close the activity
    }

    private void deleteChatHistory() {
        CollectionReference messagesRef = db.collection("chats").document(chatId).collection("messages");
        messagesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult().getDocuments()) {
                    messagesRef.document(document.getId()).delete(); // Delete each message
                }
                // Delete the parent document
                db.collection("chats").document(chatId).delete()
                        .addOnSuccessListener(aVoid -> Log.d("ChatActivity", "Parent document deleted"))
                        .addOnFailureListener(e -> Log.e("ChatActivity", "Error deleting parent document", e));
            }
        });
    }
}