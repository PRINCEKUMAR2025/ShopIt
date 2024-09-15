package com.example.princeecommerceapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.princeecommerceapp.R;
import com.example.princeecommerceapp.adapters.OrdersAdapter;
import com.example.princeecommerceapp.adapters.ProfileAddressAdapter;
import com.example.princeecommerceapp.models.AddressModel;
import com.example.princeecommerceapp.models.OrdersModel;
import com.example.princeecommerceapp.models.ProfileAddressModel;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyProfileActivty extends AppCompatActivity {
    Toolbar toolbar;
    FirebaseFirestore firestore;
    FirebaseAuth auth;
    TextView name,email;
    String UserAddress;
    EditText newname, newaddress, newcity, newpostalCode, newphoneNumber;
    Button update,videoAd;
    RecyclerView recyclerView;
    private List<ProfileAddressModel> profileModelList;
    private ProfileAddressAdapter addressAdapter;
    InterstitialAd mInterstitialAd;
    private int totalcoins = 0;
    private int pervid = 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile_activty);
        toolbar = findViewById(R.id.profile_toolbar);
        name=findViewById(R.id.name);
        email=findViewById(R.id.email);
        newname = findViewById(R.id.up_name);
        newaddress = findViewById(R.id.up_address);
        newcity = findViewById(R.id.up_city);
        newpostalCode = findViewById(R.id.up_code);
        newphoneNumber = findViewById(R.id.up_phone);
        update = findViewById(R.id.buttonUpdateAddress);
        recyclerView=findViewById(R.id.recycler_view_profile);
        videoAd=findViewById(R.id.videoAd);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        email.setText(auth.getCurrentUser().getEmail());

        fetchCurrentCoins();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // ad loading
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

       /* Fix banner ad AdView adView = new AdView(this);

        adView.setAdSize(AdSize.BANNER);

        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
// TODO: Add adView to your view hierarchy.*/
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,"ca-app-pub-8361032125437158/4624009127", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        mInterstitialAd = null;
                    }
                });

        //
        videoAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(MyProfileActivty.this);
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            mInterstitialAd = null;
                            updateCoinsAfterAd();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            mInterstitialAd = null;
                        }
                    });
                } else {
                    Toast.makeText(MyProfileActivty.this, "Ad not available at the moment..", Toast.LENGTH_SHORT).show();
                }
            }
        });



        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        profileModelList = new ArrayList<>();
        addressAdapter = new ProfileAddressAdapter(getApplicationContext(), profileModelList);
        recyclerView.setAdapter(addressAdapter);

        firestore.collection("CurrentUser").document(auth.getCurrentUser().getUid())
                .collection("Address").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                ProfileAddressModel profileModel = doc.toObject(ProfileAddressModel.class);
                                profileModelList.add(profileModel);
                                addressAdapter.notifyDataSetChanged();
                            }
                        }else {
                            Toast.makeText(MyProfileActivty.this, "You have not added address..", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = newname.getText().toString();
                String userCity = newcity.getText().toString();
                String userAddress = newaddress.getText().toString();
                String userCode = newpostalCode.getText().toString();
                String userNumber = newphoneNumber.getText().toString();

                String final_address = "";

                if (!userName.isEmpty()) {
                    final_address+="Name: "+userName;
                }
                if (!userCity.isEmpty()) {
                    final_address+=" City: "+userCity;
                }
                if (!userAddress.isEmpty()) {
                    final_address+=" Address: "+userAddress;
                }
                if (!userCode.isEmpty()) {
                    final_address+=" PostalCode: "+userCode;
                }
                if (!userNumber.isEmpty()){
                    final_address+=" Number: "+userNumber;
                }

                if (!userName.isEmpty() && !userCity.isEmpty() && !userAddress.isEmpty() && !userCode.isEmpty() && !userNumber.isEmpty()){

                    Map<String, String> map = new HashMap<>();
                    map.put("userAddress", final_address);

                    firestore.collection("CurrentUser").document(auth.getCurrentUser().getUid())
                            .collection("Address").add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(MyProfileActivty.this, "Address Added Successfully", Toast.LENGTH_SHORT).show();
                                        update.setVisibility(View.GONE);
                                        startActivity(new Intent(MyProfileActivty.this,MainActivity.class));
                                        finish();
                                    }
                                    else {
                                        Toast.makeText(MyProfileActivty.this, "Fill All the Fields..", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
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
                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                if (doc.exists()) {
                                    totalcoins = doc.getLong("amount").intValue();
                                    videoAd.setText(String.valueOf(totalcoins));
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
    private void updateCoinsAfterAd() {
        String userId = auth.getCurrentUser().getUid();
        totalcoins += pervid;
        videoAd.setText(String.valueOf(totalcoins));

        // Reference to the Coins collection
        firestore.collection("CurrentUser").document(userId).collection("Coins")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (!querySnapshot.isEmpty()) {
                                // Update the first document in the collection
                                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                    doc.getReference().update("amount", totalcoins)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d("Firestore", "DocumentSnapshot successfully updated!");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w("Firestore", "Error updating document", e);
                                                }
                                            });
                                    break; // Update only the first document
                                }
                            } else {
                                // No documents found, create a new document
                                Map<String, Object> coinData = new HashMap<>();
                                coinData.put("amount", totalcoins);
                                firestore.collection("CurrentUser").document(userId).collection("Coins")
                                        .add(coinData)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Log.d("Firestore", "DocumentSnapshot successfully created!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("Firestore", "Error creating document", e);
                                            }
                                        });
                            }
                        } else {
                            Log.d("Firestore", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


}