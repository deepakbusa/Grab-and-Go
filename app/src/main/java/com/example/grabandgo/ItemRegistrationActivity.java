package com.example.grabandgo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ItemRegistrationActivity extends AppCompatActivity {
    private EditText itemNameInput;
    private EditText itemCostInput;
    private DatabaseReference databaseReference;
    private String shopName;
    private TextView shopNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_registration);

        // Initialize views
        itemNameInput = findViewById(R.id.item_name);
        itemCostInput = findViewById(R.id.item_cost);
        shopNameTextView = findViewById(R.id.shopNameTextView);
        Button submitButton = findViewById(R.id.submit_button);
        Button logoutButton = findViewById(R.id.logout_button);

        // Get shop name from intent
        shopName = getIntent().getStringExtra("shopName");
        if (shopName != null && !shopName.isEmpty()) {
            shopNameTextView.setText(shopName);
        } else {
            Log.e("ItemRegistrationActivity", "shopName is null or empty");
            Toast.makeText(this, "Error: Shop name not provided.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("shops").child(shopName).child("items");

        // Logout button functionality
        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(ItemRegistrationActivity.this, login.class);
            startActivity(intent);
            finish();
        });

        // Submit button functionality
        submitButton.setOnClickListener(v -> {
            if (validateInputs()) {
                addItemToDatabase();
            }
        });
    }

    // Validate input fields
    private boolean validateInputs() {
        String itemName = itemNameInput.getText().toString().trim();
        String itemCost = itemCostInput.getText().toString().trim();

        if (itemName.isEmpty() || itemCost.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            Double.parseDouble(itemCost);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid cost value", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // Add item to Firebase Database
    private void addItemToDatabase() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int nextItemNumber = (int) dataSnapshot.getChildrenCount() + 1;
                String newItemKey = "item" + nextItemNumber;

                String name = itemNameInput.getText().toString().trim();
                double cost = Double.parseDouble(itemCostInput.getText().toString().trim());

                // Create item data
                Item item = new Item(name, cost);
                databaseReference.child(newItemKey).setValue(item)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ItemRegistrationActivity.this, "Item added successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ItemRegistrationActivity.this, sucess_item.class);
                            intent.putExtra("shopName", shopName);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(ItemRegistrationActivity.this, "Failed to add item: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ItemRegistrationActivity.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Item class
    public static class Item {
        public String name;
        public double price;

        public Item() {
        }

        public Item(String name, double price) {
            this.name = name;
            this.price = price;
        }
    }
}