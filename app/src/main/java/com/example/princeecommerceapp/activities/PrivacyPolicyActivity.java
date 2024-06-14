package com.example.princeecommerceapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.princeecommerceapp.R;

public class PrivacyPolicyActivity extends AppCompatActivity {

    WebView myWeb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        myWeb=findViewById(R.id.myWeb);
        myWeb.getSettings().setJavaScriptEnabled(true);
        myWeb.setWebViewClient(new WebViewClient());
        myWeb.loadUrl("https://www.freeprivacypolicy.com/live/428a0846-f3d8-4cdf-9675-52aeaae661c9");
    }
}