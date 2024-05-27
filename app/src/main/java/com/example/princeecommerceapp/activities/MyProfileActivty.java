package com.example.princeecommerceapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
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
import java.util.Map;

public class MyProfileActivty extends AppCompatActivity {
    Toolbar toolbar;
    FirebaseFirestore firestore;
    FirebaseAuth auth;
    TextView name,email;
    String UserAddress;
    EditText newname, newaddress, newcity, newpostalCode, newphoneNumber;
    Button update;
    RecyclerView recyclerView;
    private List<ProfileAddressModel> profileModelList;
    private ProfileAddressAdapter addressAdapter;
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
        email.setText(auth.getCurrentUser().getEmail());

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
}