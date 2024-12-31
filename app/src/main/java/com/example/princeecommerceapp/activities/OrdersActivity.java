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
import java.util.HashMap;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    private List<OrdersModel> orderModelList;
    private OrdersAdapter ordersAdapter;
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    Toolbar toolbar;

    TextView noOrdersText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        toolbar = findViewById(R.id.payment_toolbar);
        noOrdersText=findViewById(R.id.no_orders_text);
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
        // Fetch the user's orders from Firestore
        firestore.collection("Orders")
                .document(auth.getCurrentUser().getUid())
                .collection("NormalOrder")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Clear the previous list of orders
                            orderModelList.clear();

                            // Iterate through the documents and map them to OrdersModel
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                OrdersModel orderModel = doc.toObject(OrdersModel.class);

                                if (orderModel != null) {
                                    orderModel.setOrderId(doc.getId());
                                    orderModel.setUserOrder(doc.getString("userOrder"));
                                    orderModel.setOrderStatus(doc.getString("orderStatus"));
                                    orderModelList.add(orderModel);
                                }
                            }

                            // Notify the adapter that the data has changed
                            ordersAdapter.notifyDataSetChanged();

                            // Show or hide the "No Orders" message
                            if (orderModelList.isEmpty()) {
                                noOrdersText.setVisibility(View.VISIBLE);  // Show "No Orders" text
                                recyclerView.setVisibility(View.GONE);      // Hide RecyclerView
                            } else {
                                noOrdersText.setVisibility(View.GONE);     // Hide "No Orders" text
                                recyclerView.setVisibility(View.VISIBLE);  // Show RecyclerView
                            }
                        } else {
                            Toast.makeText(OrdersActivity.this, "Failed to fetch orders", Toast.LENGTH_SHORT).show();
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

        // Get Firestore references
        String userId = auth.getCurrentUser().getUid();

        // Ensure parent document exists
        firestore.collection("CancelledOrders").document(userId).set(new HashMap<>());

        DocumentReference orderRef = firestore.collection("Orders")
                .document(userId)
                .collection("NormalOrder")
                .document(orderModel.getOrderId());

        DocumentReference cancelledOrderRef = firestore.collection("CancelledOrders")
                .document(userId)
                .collection("CancelledOrdersByUser")
                .document(orderModel.getOrderId());

        // Write canceled order to CancelledOrders collection
        cancelledOrderRef.set(orderModel)
                .addOnSuccessListener(aVoid -> {
                    // Delete the order from NormalOrder after successful write
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
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(OrdersActivity.this, "Failed to move order to canceled list", Toast.LENGTH_SHORT).show();
                });
    }

}