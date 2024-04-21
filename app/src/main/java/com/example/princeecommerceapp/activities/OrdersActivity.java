package com.example.princeecommerceapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.princeecommerceapp.R;
import com.example.princeecommerceapp.models.AddressModel;
import com.example.princeecommerceapp.models.MyCartModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class OrdersActivity extends AppCompatActivity {

    TextView normalOrder,cartOrder;
    FirebaseFirestore firestore;
    FirebaseAuth auth;
    String NormalOrder;
    String CartOrder;
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

        firestore=FirebaseFirestore.getInstance();
        auth=FirebaseAuth.getInstance();
        normalOrder=findViewById(R.id.normal_order);
        cartOrder=findViewById(R.id.cart_order);
        getNormalOrder();
        getCartOrder();
    }
    void getNormalOrder() {
        firestore.collection("Orders")
                .document(auth.getCurrentUser().getUid())
                .collection("NormalOrder")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            StringBuilder stringBuilder = new StringBuilder();
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                String userOrder = doc.getString("userOrder");
                                stringBuilder.append(userOrder).append("\n").append("\n");
                            }
                            NormalOrder = stringBuilder.toString();
                            normalOrder.setText(NormalOrder);
                        } else {
                            Toast.makeText(OrdersActivity.this, "No Orders", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    void getCartOrder() {
        firestore.collection("Orders")
                .document(auth.getCurrentUser().getUid())
                .collection("CartOrders")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            StringBuilder stringBuilder = new StringBuilder();
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                String userOrder = doc.getString("userCartOrder");
                                stringBuilder.append(userOrder).append("\n").append("\n");
                            }
                            CartOrder = stringBuilder.toString();
                            cartOrder.setText(CartOrder);
                        } else {
                            Toast.makeText(OrdersActivity.this, "No Orders", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}