package com.example.grabandgo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserInfoActivity extends AppCompatActivity {

    private static final String TAG = "UserInfoActivity";
    private TextView tvUserName, tvUserEmail;
    private Button btnCartItems, btnViewOrders, btnLogout;
    private ImageButton btnBack, btnHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_page);

        // Initialize views
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);
        btnCartItems = findViewById(R.id.btn_cart_items);
        btnViewOrders = findViewById(R.id.btn_view_orders);
        btnLogout = findViewById(R.id.btn_logout);
        btnBack = findViewById(R.id.btn_back);
        btnHome = findViewById(R.id.btn_home);

        // Load user information
        loadUserInfo();

        // Button actions
        btnCartItems.setOnClickListener(v -> {
            Intent intent = new Intent(UserInfoActivity.this, CartActivity.class);
            startActivity(intent);
        });

        btnViewOrders.setOnClickListener(v -> {
            Intent intent = new Intent(UserInfoActivity.this, OrderHistoryActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        btnBack.setOnClickListener(v -> finish()); // Go back to the previous activity

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(UserInfoActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // ✅ Method to load user information from Firebase
    private void loadUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String displayName = user.getDisplayName();
            String email = user.getEmail();

            // Format and display the name
            if (displayName != null) {
                String[] nameParts = displayName.split(" ");
                StringBuilder firstName = new StringBuilder();

                // Remove parts containing numbers
                for (String part : nameParts) {
                    if (!part.matches(".*\\d+.*")) {  // Skip parts with digits
                        firstName.append(part).append(" ");
                    }
                }

                String formattedName = capitalize(firstName.toString().trim());
                Log.d(TAG, "Formatted Name: " + formattedName);

                // Display the name with bold label
                if (!formattedName.isEmpty()) {
                    tvUserName.setText(android.text.Html.fromHtml("<b>Username:</b> " + formattedName));
                } else {
                    tvUserName.setText(android.text.Html.fromHtml("<b>Username:</b> User"));
                }
            } else {
                Log.e(TAG, "Display Name is null");
                tvUserName.setText(android.text.Html.fromHtml("<b>Username:</b> User"));
            }

            // Display the email with bold label
            if (email != null) {
                tvUserEmail.setText(android.text.Html.fromHtml("<b>Email:</b> " + email));
            } else {
                Log.e(TAG, "Email is null");
                tvUserEmail.setText(android.text.Html.fromHtml("<b>Email:</b> Not available"));
            }

        } else {
            Log.e(TAG, "User is null");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ Method to capitalize each word properly
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }

        String[] words = str.split("\\s+");
        StringBuilder capitalized = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                capitalized.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return capitalized.toString().trim();
    }

    // ✅ Method to show logout confirmation dialog
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logout())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // ✅ Method to handle logout
    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
