package com.example.princeecommerceapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.princeecommerceapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {
    EditText name,email,password;
    ProgressBar progress;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    SharedPreferences sharedPreferences;
    TextView privacypolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

//        getSupportActionBar().hide();

        auth=FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() !=null){
            startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
            finish();
        }
        name=findViewById(R.id.name);
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        progress=findViewById(R.id.progress);
        privacypolicy=findViewById(R.id.policy_tv);

        privacypolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(RegistrationActivity.this,PrivacyPolicyActivity.class);
                startActivity(intent);
            }
        });

        sharedPreferences = getSharedPreferences("onBoardingScreen",MODE_PRIVATE);

        boolean isFirstTime  =  sharedPreferences.getBoolean("firstTime",true);
        if (isFirstTime){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("firstTime",false);
            editor.commit();

            Intent intent = new Intent(RegistrationActivity.this,OnBoardingActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void signup(View view) {
        progress.setVisibility(View.VISIBLE);
        String userName = name.getText().toString();
        String userEmail = email.getText().toString();
        String userPassword = password.getText().toString();
        if (TextUtils.isEmpty(userName)){
                name.setError("Name Required");
                return;
        }
        if (TextUtils.isEmpty(userEmail)){
            email.setError("Email Required");
            return;
        }
        if (TextUtils.isEmpty(userPassword)){
            password.setError("Set your password");
        }
        if (userPassword.length()<6){
            Toast.makeText(this, "Password too short, enter atleast 6 character..", Toast.LENGTH_SHORT).show();
            return;
        }
        auth.createUserWithEmailAndPassword(userEmail,userPassword).
                addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progress.setVisibility(View.INVISIBLE);
                        if (task.isSuccessful()){
                            String userId = auth.getCurrentUser().getUid();
                            Map<String, Object> totalcoins = new HashMap<>();
                            totalcoins.put("amount", 10);

                            firestore.collection("CurrentUser").document(userId)
                                    .collection("Coins")
                                    .add(totalcoins)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId());
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("Firestore", "Error adding document", e);
                                        }
                                    });

                            Toast.makeText(RegistrationActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                            finish();
                        }else {
                            Toast.makeText(RegistrationActivity.this, "Registration Failed: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public void signin(View view) {
        startActivity(new Intent(RegistrationActivity.this,LoginActivity.class));
    }
}