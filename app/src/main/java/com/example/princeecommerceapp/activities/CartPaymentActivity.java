package com.example.princeecommerceapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.princeecommerceapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CartPaymentActivity extends AppCompatActivity {

    PaymentSheet paymentSheet;
    String paymentIntentClientSecret;
    PaymentSheet.CustomerConfiguration configuration;

    Toolbar toolbar;
    TextView subTotal,discount,shipping,total;
    int subtotal,dis,ship,totalamount;
    String Final_cart_order;
    FirebaseAuth auth;
    FirebaseFirestore firestore;

    Button cartPay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_payment);
        fetchApi();
        auth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();
        cartPay=findViewById(R.id.pay_btn);

        toolbar = findViewById(R.id.payment_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        int amount = 0;
        amount = getIntent().getIntExtra("cartTotalPrice",0);
        Final_cart_order = getIntent().getStringExtra("cartData");
        Log.e("Items",Final_cart_order);
        subtotal=amount;
        dis=-599;
        ship=199;
        totalamount = subtotal+dis+ship;
        subTotal = findViewById(R.id.sub_total);
        discount = findViewById(R.id.textView17);
        shipping = findViewById(R.id.textView18);
        total = findViewById(R.id.total_amt);

        subTotal.setText("Rs "+amount);
        total.setText("Rs "+totalamount);


        cartPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchApi();
                if (paymentIntentClientSecret !=null) {
                    paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret,
                            new PaymentSheet.Configuration("Shopit", configuration));
                }else {
                    Toast.makeText(CartPaymentActivity.this, "Initiating Payment..Try Again", Toast.LENGTH_SHORT).show();
                }
            }
        });
        paymentSheet =new PaymentSheet(this, this::onPaymentSheetResult);
    }
    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled){
            Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show();
            placeCartOrder();
            deleteFromCart();
        }
        if (paymentSheetResult instanceof PaymentSheetResult.Failed){
            Toast.makeText(this, ((PaymentSheetResult.Failed)paymentSheetResult).getError().getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (paymentSheetResult instanceof PaymentSheetResult.Completed){
            Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show();
            placeCartOrder();
            deleteFromCart();
        }
    }

    public void fetchApi(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://stripepaymentmooviz.000webhostapp.com/index.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response);
                            configuration =new PaymentSheet.CustomerConfiguration(
                                    jsonObject.getString("customer"),
                                    jsonObject.getString("ephemeralKey")
                            );
                            paymentIntentClientSecret= jsonObject.getString("paymentIntent");
                            PaymentConfiguration.init(getApplicationContext(),jsonObject.getString("publishableKey"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               error.printStackTrace();
            }
        }){
            protected Map<String, String> getParams(){
                Map<String, String> paramV = new HashMap<>();
                paramV.put("authKey", "abc");
                return paramV;
            }
        };
        queue.add(stringRequest);
    }

    void placeCartOrder(){
        Map<String, String> map = new HashMap<>();
        map.put("userCartOrder",Final_cart_order);

        firestore.collection("Orders").document(auth.getCurrentUser().getUid())
                .collection("CartOrders").add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(CartPaymentActivity.this, "Order Placed Successfully..", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(CartPaymentActivity.this, OrderPlacedActivity.class));
                            finish();
                        }
                        else {
                            Toast.makeText(CartPaymentActivity.this, "Order Failed..", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    void deleteFromCart(){
        firestore.collection("AddToCart")
                .document(auth.getCurrentUser().getUid())
                .collection("User")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                // Get the document ID for each cart item
                                String docId = doc.getId();

                                // Delete the cart item document
                                firestore.collection("AddToCart")
                                        .document(auth.getCurrentUser().getUid())
                                        .collection("User")
                                        .document(docId)
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.e("Delete item","Cart Flushed");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("Delete item","Cart Flush Failed");
                                            }
                                        });
                            }
                        }
                    }
                });
    }
}