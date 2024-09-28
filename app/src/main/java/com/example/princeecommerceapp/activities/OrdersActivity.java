package com.example.princeecommerceapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.princeecommerceapp.R;
import com.example.princeecommerceapp.adapters.OrdersAdapter;
import com.example.princeecommerceapp.models.OrdersModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    private List<OrdersModel> orderModelList;
    private OrdersAdapter ordersAdapter;
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        toolbar = findViewById(R.id.payment_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.normal_order_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        orderModelList = new ArrayList<>();
        ordersAdapter = new OrdersAdapter(getApplicationContext(), orderModelList);
        recyclerView.setAdapter(ordersAdapter);

        // Fetch orders from Firestore
        fetchOrders();

        // Set long click listener for canceling orders
        ordersAdapter.setOnItemLongClickListener(new OrdersAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(OrdersModel orderModel) {
                showCancelConfirmationDialog(orderModel); // Show confirmation dialog
            }
        });
    }

    private void fetchOrders() {
        firestore.collection("Orders").document(auth.getCurrentUser().getUid())
                .collection("NormalOrder").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                OrdersModel orderModel = doc.toObject(OrdersModel.class);
                                if (orderModel != null) {
                                    orderModel.setOrderId(doc.getId()); // Set the document ID as orderId
                                    orderModelList.add(orderModel);
                                }
                            }
                            ordersAdapter.notifyDataSetChanged(); // Notify adapter after adding all orders
                        } else {
                            Toast.makeText(OrdersActivity.this, "You have no orders", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showCancelConfirmationDialog(OrdersModel orderModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Inflate the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cancel_order, null);
        builder.setView(dialogView);

        // Find the buttons in the custom layout
        Button buttonYes = dialogView.findViewById(R.id.button_yes);
        Button buttonNo = dialogView.findViewById(R.id.button_no);

        // Set up the dialog title and message
        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);

        // Create the dialog
        AlertDialog dialog = builder.create();

        // Set button click listeners
        buttonYes.setOnClickListener(v -> {
            cancelOrder(orderModel); // Proceed to cancel
            dialog.dismiss(); // Dismiss the dialog after action
        });

        buttonNo.setOnClickListener(v -> {
            // Dismiss the dialog
            dialog.dismiss();
        });

        // Show the dialog
        dialog.show();
    }

    private void cancelOrder(OrdersModel orderModel) {
        if (orderModel == null || orderModel.getOrderId() == null) {
            Toast.makeText(this, "Invalid order. Cannot cancel.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the document reference for the order to be deleted
        DocumentReference orderRef = firestore.collection("Orders")
                .document(auth.getCurrentUser().getUid())
                .collection("NormalOrder")
                .document(orderModel.getOrderId());

        orderRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    orderModelList.remove(orderModel); // Remove from local list
                    ordersAdapter.notifyDataSetChanged(); // Notify adapter
                    Toast.makeText(OrdersActivity.this, "Order canceled successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(OrdersActivity.this, "Failed to cancel order", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}