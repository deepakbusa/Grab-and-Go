package com.example.grabandgo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class login extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        usernameEditText = findViewById(R.id.editTextTextUsername);
        passwordEditText = findViewById(R.id.editTextTextPassword);
        Button loginButton = findViewById(R.id.buttonLogin);
        Button Goback = findViewById(R.id.goback);

        // Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("shops");

        // Login button logic
        loginButton.setOnClickListener(v -> {
            String enteredUsername = usernameEditText.getText().toString().trim();
            String enteredPassword = passwordEditText.getText().toString().trim();

            if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
                Toast.makeText(login.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            authenticateShop(enteredUsername, enteredPassword);
        });

        // Go back to the first page
        Goback.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, firstpage.class);
            startActivity(intent);
            finish();
        });
    }

    // Authenticate shop using Firebase
    private void authenticateShop(String username, String password) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean loginSuccess = false;

                for (DataSnapshot shopSnapshot : dataSnapshot.getChildren()) {
                    String dbUsername = shopSnapshot.child("User").getValue(String.class);
                    String dbPassword = shopSnapshot.child("Pass").getValue(String.class);

                    if (username.equals(dbUsername) && password.equals(dbPassword)) {
                        loginSuccess = true;
                        String shopName = shopSnapshot.getKey();

                        // Save shop name using SharedPreferences
                        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("shopName", shopName);
                        editor.apply();

                        // Redirect to HomeActivity
                        Intent intent = new Intent(login.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                }

                if (!loginSuccess) {
                    Toast.makeText(login.this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(login.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}