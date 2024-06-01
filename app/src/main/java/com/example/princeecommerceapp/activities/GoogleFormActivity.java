package com.example.princeecommerceapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.princeecommerceapp.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.Map;

public class GoogleFormActivity extends AppCompatActivity {

    EditText edtName, edtPhone;
    ProgressBar progressDialog;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_form);
        edtName = findViewById(R.id.edt_Name);
        edtPhone = findViewById(R.id.edt_Phone);
        progressDialog = findViewById(R.id.progress_Bar);
        btn=findViewById(R.id.btnSubmit);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postData(edtName.getText().toString().trim(),edtPhone.getText().toString().trim());
            }
        });
    }

    public void postData(final String name, final String phone) {
        progressDialog.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                Constants.url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("TAG", "Response: " + response);
                        if (response.length() > 0) {
                            Toast.makeText(GoogleFormActivity.this, "Successfully Posted", Toast.LENGTH_SHORT).show();
                            edtName.setText(null);
                            edtPhone.setText(null);
                        } else {
                            Toast.makeText(GoogleFormActivity.this, "Try Again", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.setVisibility(View.INVISIBLE);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.setVisibility(View.INVISIBLE);
                Toast.makeText(GoogleFormActivity.this, "Error while posting the form..", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(Constants.nameField, name);
                params.put(Constants.phoneField, phone);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}