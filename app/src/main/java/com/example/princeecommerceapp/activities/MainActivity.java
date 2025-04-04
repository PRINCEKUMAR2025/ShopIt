package com.example.princeecommerceapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.princeecommerceapp.R;
import com.example.princeecommerceapp.fragments.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    Fragment homeFragment;
    Toolbar toolbar;
    FirebaseAuth auth;
    BottomNavigationView nav;
    ProgressBar progressBar;

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            int permissionState = ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS);
            if (permissionState == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseMessaging.getInstance().subscribeToTopic(userId)
                .addOnCompleteListener(task -> {
                    String msg = "Subscribed to order updates :"+ userId;
                    if (!task.isSuccessful()) {
                        msg = "Subscription failed";
                    }
                    Log.d("FCM", msg);
                });

        auth=FirebaseAuth.getInstance();
        toolbar=findViewById(R.id.home_toolbar);
        nav=findViewById(R.id.bottom_navigation);
        progressBar=findViewById(R.id.main_progress);

        setSupportActionBar(toolbar);
        homeFragment = new HomeFragment();
        loadFragment(homeFragment);

        nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId()==R.id.home){
                    progressBar.setVisibility(View.VISIBLE);
                    Intent intent=new Intent(MainActivity.this,MainActivity.class);
                    startActivity(intent);
                    Toast.makeText(MainActivity.this, "Refreshed..", Toast.LENGTH_SHORT).show();
                    finish();
                }
                if (item.getItemId()==R.id.menu_my_cart){
                    Intent intent=new Intent(MainActivity.this,CartActivity.class);
                    startActivity(intent);
                }
                if (item.getItemId()==R.id.menu_orders){
                    Intent intent=new Intent(MainActivity.this,OrdersActivity.class);
                    startActivity(intent);
                }
                if (item.getItemId()==R.id.profile){
                    Intent intent=new Intent(MainActivity.this,MyProfileActivty.class);
                    startActivity(intent);
                }
                if (item.getItemId()==R.id.chatbot){
                    Intent intent=new Intent(MainActivity.this,ChatBot.class);
                    startActivity(intent);
                }
                return true;
            }
        });
    }

    private void loadFragment(Fragment homeFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.home_container,homeFragment);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id==R.id.menu_logout){
            auth.signOut();
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            Toast.makeText(this, "Signing Out..", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (id==R.id.menu_videos){
            startActivity(new Intent(MainActivity.this,Showvideo.class));
        }
        if (id==R.id.menu_KIWI){
            startActivity(new Intent(MainActivity.this, KiwiActivity.class));
        }
        return true;
    }

    // Handling the result of notification permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification Permission is Required", Toast.LENGTH_SHORT).show();
            }
        }
    }

}