package com.example.princeecommerceapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.princeecommerceapp.R;
import com.example.princeecommerceapp.adapters.OrdersAdapter;
import com.example.princeecommerceapp.models.OrdersModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

        firestore.collection("Orders").document(auth.getCurrentUser().getUid())
                .collection("NormalOrder").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                OrdersModel orderModel = doc.toObject(OrdersModel.class);
                                orderModelList.add(orderModel);
                                ordersAdapter.notifyDataSetChanged();
                            }
                        }else {
                            Toast.makeText(OrdersActivity.this, "You have no orders", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
