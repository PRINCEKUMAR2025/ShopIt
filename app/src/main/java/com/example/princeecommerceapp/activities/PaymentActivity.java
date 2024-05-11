package com.example.princeecommerceapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.princeecommerceapp.R;
import com.example.princeecommerceapp.models.AddressModel;
import com.google.android.gms.tasks.OnCompleteListener;
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

public class PaymentActivity extends AppCompatActivity {

    //payment integration
    PaymentSheet paymentSheet;
    String paymentIntentClientSecret;
    PaymentSheet.CustomerConfiguration configuration;

    Toolbar toolbar;
    TextView subTotal,discount,shipping,total;
    int subtotal,dis,ship,totalamount;
    FirebaseFirestore firestore;
    FirebaseAuth auth;
    Button payBtn;
    int amount = 0;
    String address;
    String productName;
    String final_Order ="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        fetchApi();
        firestore=FirebaseFirestore.getInstance();
        auth=FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.payment_toolbar);
        payBtn = findViewById(R.id.pay_btn);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });



        amount = getIntent().getIntExtra("amount",0);
        address = getIntent().getStringExtra("address");
        productName = getIntent().getStringExtra("productName");
        final_Order = "Item Name-> " +productName+"\n"+" DeliveryAddress-> "+"["+address+"]"+"\n"+"ProductPrice-> "+amount;

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


        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchApi();
                if (paymentIntentClientSecret !=null) {
                    paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret,
                            new PaymentSheet.Configuration("Shopit", configuration));
                }else {
                    Toast.makeText(PaymentActivity.this, "Initiating Payment..Try Again", Toast.LENGTH_SHORT).show();
                }
            }
        });
        paymentSheet =new PaymentSheet(this, this::onPaymentSheetResult);
    }

    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled){
            Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show();
            placeOrder();
        }
        if (paymentSheetResult instanceof PaymentSheetResult.Failed){
            Toast.makeText(this, ((PaymentSheetResult.Failed)paymentSheetResult).getError().getMessage(),Toast.LENGTH_SHORT).show();
        }
        if (paymentSheetResult instanceof PaymentSheetResult.Completed){
            Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show();
            placeOrder();
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
                            paymentIntentClientSecret = jsonObject.getString("paymentIntent");
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

    void placeOrder(){
        Map<String, String> map = new HashMap<>();
        map.put("userOrder", final_Order);

        firestore.collection("Orders").document(auth.getCurrentUser().getUid())
                .collection("NormalOrder").add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(PaymentActivity.this, "Order Placed Successfully..", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(PaymentActivity.this,OrderPlacedActivity.class));
                            finish();
                        }
                        else {
                            Toast.makeText(PaymentActivity.this, "Order Failed..", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}