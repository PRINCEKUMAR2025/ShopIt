package com.example.princeecommerceapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseMessaging.getInstance().subscribeToTopic("shopit");

        auth=FirebaseAuth.getInstance();
        toolbar=findViewById(R.id.home_toolbar);
        nav=findViewById(R.id.bottom_navigation);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);
        homeFragment = new HomeFragment();
        loadFragment(homeFragment);

        nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId()==R.id.home){

                    Toast.makeText(MainActivity.this, "Reloading...", Toast.LENGTH_SHORT).show();
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
        return true;
    }
}