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

import com.android.volley.AuthFailureError;
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

    Toolbar toolbar;
    TextView subTotal,discount,shipping,total;
    int subtotal,dis,ship,totalamount;
    String Final_cart_order;
    FirebaseAuth auth;
    FirebaseFirestore firestore;

    Button cartPay;
    String totalcartpay;
    String cartpayconv;
    String SECRET_KEY = "sk_test_51Os3QnSGRnnzLmir6qr1NcJJrcHJaZtvt7tOV9suMGRklSkaCtEbYeQNYTJfBImKhwLzndje57CqP22283OaB0fH009sux7Cta";
    String PUBLISH_KEY="pk_test_51Os3QnSGRnnzLmir77n9qIxmPS3WcbFy4DEQ5jiu7H1RBx5ud7Uzqj0R42zo7mMi4RLlI0X3lFGicbDTpeZuFr5d00ylj0mYBh";
    String customerID;
    String EmphericalKey;
    String ClientSecret;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_payment);
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
        cartpayconv=String.valueOf(totalamount);
        totalcartpay=cartpayconv+"00";
        subTotal = findViewById(R.id.sub_total);
        discount = findViewById(R.id.textView17);
        shipping = findViewById(R.id.textView18);
        total = findViewById(R.id.total_amt);

        subTotal.setText("Rs "+amount);
        total.setText("Rs "+totalamount);

        PaymentConfiguration.init(this,PUBLISH_KEY);
        paymentSheet=new PaymentSheet(this, this::onPaymentResult);

        cartPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PaymentFlow();
            }
        });

        StringRequest stringRequest=new StringRequest(Request.Method.POST
                , "https://api.stripe.com/v1/customers", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object=new JSONObject(response);
                    customerID=object.getString("id");
                    Log.e("payment info",customerID);
                    getEmphericalKey(customerID);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> header=new HashMap<>();
                header.put("Authorization","Bearer "+SECRET_KEY);
                return header;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(CartPaymentActivity.this);
        requestQueue.add(stringRequest);
    }

    private void getEmphericalKey(String customerID) {

        StringRequest stringRequest=new StringRequest(Request.Method.POST
                , "https://api.stripe.com/v1/ephemeral_keys", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object=new JSONObject(response);
                    EmphericalKey=object.getString("id");
                    Log.e("payment info",EmphericalKey);
                    getClientSecret(customerID,EmphericalKey);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> header=new HashMap<>();
                header.put("Stripe-Version","2020-08-27");
                header.put("Authorization","Bearer "+SECRET_KEY);
                return header;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params=new HashMap<>();
                params.put("customer",customerID);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(CartPaymentActivity.this);
        requestQueue.add(stringRequest);

    }

    private void getClientSecret(String customerID, String emphericalKey) {
        StringRequest stringRequest=new StringRequest(Request.Method.POST
                , "https://api.stripe.com/v1/payment_intents", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object=new JSONObject(response);
                    ClientSecret=object.getString("client_secret");
                    Log.e("payment info",ClientSecret);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> header=new HashMap<>();
                header.put("Authorization","Bearer "+SECRET_KEY);
                return header;
            }


            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params=new HashMap<>();
                params.put("customer",customerID);
                params.put("amount",totalcartpay);
                params.put("currency","inr");
                params.put("automatic_payment_methods[enabled]","true");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(CartPaymentActivity.this);
        requestQueue.add(stringRequest);
    }

    private void PaymentFlow() {

        paymentSheet.presentWithPaymentIntent(
                ClientSecret,new PaymentSheet.Configuration("Shopit" ,
                        new PaymentSheet.CustomerConfiguration(customerID,EmphericalKey))
        );
    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled){
            Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show();
        }
        if (paymentSheetResult instanceof PaymentSheetResult.Failed){
            Toast.makeText(this, ((PaymentSheetResult.Failed)paymentSheetResult).getError().getMessage(),Toast.LENGTH_SHORT).show();
        }
        if (paymentSheetResult instanceof PaymentSheetResult.Completed){
            Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show();
            placeCartOrder();
            deleteFromCart();
        }
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