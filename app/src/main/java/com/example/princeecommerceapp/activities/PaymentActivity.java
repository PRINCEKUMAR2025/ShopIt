package com.example.princeecommerceapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
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
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.PaymentDataRequest;

public class PaymentActivity extends AppCompatActivity {

    // Payment integration
    PaymentSheet paymentSheet;

    // Google Pay integration
    private static final int REQUEST_CODE_PAYMENT = 123; // Define a request code for payment
    private PaymentsClient paymentsClient;

    Toolbar toolbar;
    TextView subTotal, discount, shipping, total, coinsview;
    int subtotal, dis, ship, totalamount, coins;
    String totalpay;
    String totalamounttopay;
    FirebaseFirestore firestore;
    FirebaseAuth auth;
    Button payBtn;
    int amount = 0;
    String address;
    String productName;
    String final_Order = "";
    String SECRET_KEY = "sk_test_51Os3QnSGRnnzLmir6qr1NcJJrcHJaZtvt7tOV9suMGRklSkaCtEbYeQNYTJfBImKhwLzndje57CqP22283OaB0fH009sux7Cta";
    String PUBLISH_KEY = "pk_test_51Os3QnSGRnnzLmir77n9qIxmPS3WcbFy4DEQ5jiu7H1RBx5ud7Uzqj0R42zo7mMi4RLlI0X3lFGicbDTpeZuFr5d00ylj0mYBh";
    String customerID;
    String EmphericalKey;
    String ClientSecret;
    MediaPlayer player;
    ImageButton gpay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Initialize Google Pay API
        Wallet.WalletOptions walletOptions = new Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST) // Use ENVIRONMENT_PRODUCTION for live
                .build();
        paymentsClient = Wallet.getPaymentsClient(this, walletOptions);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        fetchCurrentCoins();
        toolbar = findViewById(R.id.payment_toolbar);
        payBtn = findViewById(R.id.pay_btn);
        coinsview = findViewById(R.id.coinsview);
        gpay = findViewById(R.id.pay_gpay);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        amount = getIntent().getIntExtra("amount", 0);
        address = getIntent().getStringExtra("address");
        productName = getIntent().getStringExtra("productName");
        final_Order = "Item Name-> " + productName + "\n" + " DeliveryAddress-> " + "[" + address + "]" + "\n" + "ProductPrice-> " + amount;

        subtotal = amount;
        dis = -599;
        ship = 199;

        totalamount = subtotal + dis + ship;
        totalpay = String.valueOf(totalamount);
        totalamounttopay = totalpay + "00";

        subTotal = findViewById(R.id.sub_total);
        discount = findViewById(R.id.textView17);
        shipping = findViewById(R.id.textView18);
        total = findViewById(R.id.total_amt);

        subTotal.setText("Rs " + amount);
        total.setText("Rs " + totalamount);

        // NEW STRIPE
        PaymentConfiguration.init(this, PUBLISH_KEY);
        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PaymentFlow();
            }
        });

        StringRequest stringRequest = new StringRequest(Request.Method.POST
                , "https://api.stripe.com/v1/customers", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    customerID = object.getString("id");
                    Log.e("payment info", customerID);
                    getEmphericalKey(customerID);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SECRET_KEY);
                return header;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(PaymentActivity.this);
        requestQueue.add(stringRequest);

        // Set up Google Pay button click listener
        gpay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPayment(); // Call the method to request payment
            }
        });
    }

    private void requestPayment() {
        // Create a PaymentDataRequest object
        PaymentDataRequest request = createPaymentDataRequest();
        if (request != null) {
            // Start the Google Pay payment flow
            AutoResolveHelper.resolveTask(
                    paymentsClient.loadPaymentData(request),
                    this,
                    REQUEST_CODE_PAYMENT);
        }
    }

    private PaymentDataRequest createPaymentDataRequest() {
        // Create a PaymentDataRequest object
        return PaymentDataRequest.fromJson("{\n" +
                "  \"apiVersion\": 2,\n" +
                "  \"apiVersionMinor\": 0,\n" +
                "  \"merchantInfo\": {\n" +
                "    \"merchantId\": \"1906-0514-3188\",\n" +
                "    \"merchantName\": \"Prince\"\n" +
                "  },\n" +
                "  \"transactionInfo\": {\n" +
                "    \"totalPriceStatus\": \"FINAL\",\n" +
                "    \"totalPrice\": \"" + totalamounttopay + "\",\n" +
                "    \"currencyCode\": \"INR\",\n" +
                "    \"countryCode\": \"IN\"\n" +
                "  },\n" +
                "  \"cardRequirements\": {\n" +
                "    \"allowedCardNetworks\": [\"VISA\", \"MASTERCARD\"],\n" +
                "    \"billingAddressRequired\": true,\n" +
                "    \"billingAddressParameters\": {\n" +
                "      \"format\": \"FULL\"\n" +
                "    }\n" +
                "  }\n" +
                "}");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == RESULT_OK && data != null) {
                // Payment was successful
                PaymentData paymentData = PaymentData.getFromIntent(data);
                handlePaymentSuccess(paymentData);
            } else {
                // Payment failed or was canceled
                Log.e("PaymentActivity", "Payment failed: " + resultCode);
                Toast.makeText(this, "Payment failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handlePaymentSuccess(PaymentData paymentData) {
        if (paymentData != null) {
            // Get the payment data as a JSON string
            String paymentDataJson = paymentData.toJson();
            Log.d("PaymentActivity", "Payment Data: " + paymentDataJson);

            // You can parse the JSON string to extract specific fields if needed
            // For example, you can extract the payment token or other details

            // Proceed with your order processing logic
            placeOrder(); // Call placeOrder() to save the order
            resetCoinsToZero(); // Reset coins after successful payment
        } else {
            Log.e("PaymentActivity", "Payment data is null");
            Toast.makeText(this, "Payment data is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void getEmphericalKey(String customerID) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST
                , "https://api.stripe.com/v1/ephemeral_keys", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    EmphericalKey = object.getString("id");
                    Log.e("payment info", EmphericalKey);
                    getClientSecret(customerID, EmphericalKey);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Stripe-Version", "2020-08-27");
                header.put("Authorization", "Bearer " + SECRET_KEY);
                return header;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", customerID);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(PaymentActivity.this);
        requestQueue.add(stringRequest);
    }

    private void getClientSecret(String customerID, String emphericalKey) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST
                , "https://api.stripe.com/v1/payment_intents", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    ClientSecret = object.getString("client_secret");
                    Log.e("payment info", ClientSecret);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + SECRET_KEY);
                return header;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", customerID);
                params.put("amount", totalamounttopay);
                params.put("currency", "inr");
                params.put("automatic_payment_methods[enabled]", "true");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(PaymentActivity.this);
        requestQueue.add(stringRequest);
    }

    private void PaymentFlow() {
        paymentSheet.presentWithPaymentIntent(
                ClientSecret, new PaymentSheet.Configuration("Shopit",
                        new PaymentSheet.CustomerConfiguration(customerID, EmphericalKey))
        );
    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show();
        }
        if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Toast.makeText(this, ((PaymentSheetResult.Failed) paymentSheetResult).getError().getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            player = MediaPlayer.create(PaymentActivity.this, R.raw.paysound);
            player.start();
            Toast.makeText(this, "Payment Success", Toast.LENGTH_SHORT).show();
            placeOrder();
            resetCoinsToZero();
        }
    }

    void placeOrder() {
        // Create a map to hold the order data
        Map<String, String> map = new HashMap<>();
        map.put("userOrder", final_Order);
        map.put("orderStatus", "Placed"); // Add the orderStatus field and set it to "Placed"

        // Create the parent document if it doesn't exist
        firestore.collection("Orders").document(auth.getCurrentUser().getUid())
                .set(new HashMap<>()); // This creates the document with an empty map

        // Now add the order to the NormalOrder subcollection
        firestore.collection("Orders").document(auth.getCurrentUser().getUid())
                .collection("NormalOrder").add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(PaymentActivity.this, "Order Placed Successfully..", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(PaymentActivity.this, OrderPlacedActivity.class));
                            finish();
                        } else {
                            Toast.makeText(PaymentActivity.this, "Order Failed..", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void fetchCurrentCoins() {
        String userId = auth.getCurrentUser().getUid();
        firestore.collection("CurrentUser").document(userId)
                .collection("Coins").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("Firestore", "Task successful");
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                if (doc.exists()) {
                                    Log.d("Firestore", "Document exists");
                                    coins = doc.getLong("amount").intValue();
                                    Log.d("Firestore", "Coins value: " + coins);
                                    // Update coinsview here
                                    coinsview.setText(String.valueOf(-coins));
                                    // Update total amount
                                    totalamount = subtotal + dis + ship - coins;
                                    totalpay = String.valueOf(totalamount);
                                    totalamounttopay = totalpay + "00";
                                    total.setText("Rs " + totalamount);
                                } else {
                                    Log.d("Firestore", "No such document");
                                }
                            }
                        } else {
                            Log.d("Firestore", "get failed with ", task.getException());
                        }
                    }
                });
    }

    private void resetCoinsToZero() {
        String userId = auth.getCurrentUser().getUid();
        firestore.collection("CurrentUser").document(userId)
                .collection("Coins").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                if (doc.exists()) {
                                    // Update the amount to 0
                                    DocumentReference docRef = doc.getReference();
                                    docRef.update("amount", 0)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d("Firestore", "Coins successfully reset to 0");
                                                    coinsview.setText("0");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w("Firestore", "Error updating document", e);
                                                }
                                            });
                                } else {
                                    Log.d("Firestore", "No such document");
                                }
                            }
                        } else {
                            Log.d("Firestore", "get failed with ", task.getException());
                        }
                    }
                });
    }
}