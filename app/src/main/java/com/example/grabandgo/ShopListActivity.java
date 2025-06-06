package com.example.grabandgo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;  // Import for loading spinner
import androidx.appcompat.widget.SearchView; // Use this import for SearchView
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShopListActivity extends AppCompatActivity {

    private static final String TAG = "ShopListActivity";
    private LinearLayout shopListContainer;
    private DatabaseReference databaseReference;
    private TextView tvUserName;
    private ProgressBar loadingSpinner; // Loading spinner
    private List<String> shopNames;
    private List<String> filteredShopNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_list);

        shopListContainer = findViewById(R.id.shop_list_container);
        tvUserName = findViewById(R.id.tv_welcome);
        loadingSpinner = findViewById(R.id.loading_spinner);  // Initialize the spinner

        databaseReference = FirebaseDatabase.getInstance().getReference();

        shopNames = new ArrayList<>();
        filteredShopNames = new ArrayList<>();

        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterShops(newText);
                return true;
            }
        });

        fetchShops();

        ImageButton btnCart = findViewById(R.id.btn_cart);
        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(ShopListActivity.this, CartActivity.class);
            startActivity(intent);
        });

        ImageButton btnProfile = findViewById(R.id.btn_profile);
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ShopListActivity.this, UserInfoActivity.class);
            startActivity(intent);
        });

        ImageButton btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        displayUserInfo();
    }

    private void fetchShops() {
        // Show the loading spinner before fetching shops
        loadingSpinner.setVisibility(View.VISIBLE);

        databaseReference.child("shops").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                shopNames.clear();
                filteredShopNames.clear();
                shopListContainer.removeAllViews();

                for (DataSnapshot shopSnapshot : snapshot.getChildren()) {
                    String shopName = shopSnapshot.getKey();
                    shopNames.add(shopName);
                    filteredShopNames.add(shopName);
                    addShopToLayout(shopName);
                }

                // Hide the spinner once data is loaded
                loadingSpinner.setVisibility(View.GONE);

                if (shopNames.isEmpty()) {
                    Toast.makeText(ShopListActivity.this, "No shops found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingSpinner.setVisibility(View.GONE);
                Toast.makeText(ShopListActivity.this, "Failed to load shops", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addShopToLayout(String shopName) {
        View shopItemView = LayoutInflater.from(this).inflate(R.layout.shop_list_item, shopListContainer, false);

        TextView shopNameTextView = shopItemView.findViewById(R.id.shop_name);
        shopNameTextView.setText(shopName);

        Button viewItemsButton = shopItemView.findViewById(R.id.shop_button);
        viewItemsButton.setOnClickListener(view -> {
            Intent intent = new Intent(ShopListActivity.this, ShopItemsActivity.class);
            intent.putExtra("shopName", shopName);
            startActivity(intent);
        });

        shopListContainer.addView(shopItemView);
    }

    private void filterShops(String query) {
        shopListContainer.removeAllViews();
        filteredShopNames.clear();

        for (String shopName : shopNames) {
            if (shopName.toLowerCase().contains(query.toLowerCase())) {
                filteredShopNames.add(shopName);
                addShopToLayout(shopName);
            }
        }

        if (filteredShopNames.isEmpty()) {
            TextView noResults = new TextView(this);
            noResults.setText("No results found");
            shopListContainer.addView(noResults);
        }
    }

    private void displayUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String displayName = user.getDisplayName();
            String email = user.getEmail();

            if (displayName != null) {
                String[] nameParts = displayName.split(" ");
                StringBuilder firstName = new StringBuilder();

                for (String part : nameParts) {
                    if (part.matches(".*\\d{4}.*")) {
                        break;
                    }
                    firstName.append(part).append(" ");
                }

                String formattedName = capitalize(firstName.toString().trim());
                Log.d("UserInfo", "Formatted Name: " + formattedName);
                tvUserName.setText("Welcome, " + formattedName);
            } else {
                Log.e("UserInfo", "Display Name is null");
                tvUserName.setText("Welcome, User");
            }

            if (email != null) {
                Log.d("UserInfo", "Email: " + email);
            } else {
                Log.e("UserInfo", "Email is null");
            }
        } else {
            Log.e("UserInfo", "User is null");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

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

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    logout();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
