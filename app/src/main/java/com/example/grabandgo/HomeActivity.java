package com.example.grabandgo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView welcomeTextView = findViewById(R.id.welcomeTextView);
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String shopName = preferences.getString("shopName", "Shop Name");
        welcomeTextView.setText("WELCOME\n" + shopName);

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, login.class);
                startActivity(intent);
                finish();
            }
        });

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ItemRegistrationActivity.class);
                intent.putExtra("shopName", shopName);
                startActivity(intent);
            }
        });

        Button itemsButton = findViewById(R.id.seeItemsButton);
        itemsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, items_details.class);
                intent.putExtra("shopName", shopName);
                startActivity(intent);
            }
        });
    }
}
