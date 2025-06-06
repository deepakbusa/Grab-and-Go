package com.example.grabandgo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShopItemsActivity extends AppCompatActivity {

    private LinearLayout shopItemsContainer; // Layout to hold your item views
    private DatabaseReference databaseReference; // Firebase reference
    private String shopName; // Store the shop name
    private List<Item> itemList; // List to store fetched items
    private ProgressBar loadingSpinner; // Loading spinner

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shop_items); // Ensure this is the correct layout

        // Initialize views
        shopItemsContainer = findViewById(R.id.shop_items_container);
        ImageButton btnBack = findViewById(R.id.btn_back);
        ImageButton btnAddCart = findViewById(R.id.btn_add_cart);
        EditText searchBar = findViewById(R.id.search_bar);
        TextView tvHeading = findViewById(R.id.tv_heading);
        ImageButton btnHome = findViewById(R.id.btn_home);
        ImageButton btnAccount = findViewById(R.id.btn_account);
        loadingSpinner = findViewById(R.id.loading_spinner);  // Initialize the spinner

        // Get the shop name passed from the previous activity
        shopName = getIntent().getStringExtra("shopName");
        tvHeading.setText(shopName + " Items"); // Set the heading for the shop

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize item list
        itemList = new ArrayList<>();

        // Show spinner before loading items
        loadingSpinner.setVisibility(View.VISIBLE);

        // Fetch items for the selected shop
        fetchItems();

        // Search bar functionality
        searchBar.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterItems(charSequence.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable editable) {}
        });

        // Back button functionality
        btnBack.setOnClickListener(view -> finish());

        // Add cart button functionality
        btnAddCart.setOnClickListener(view -> {
            Intent intent = new Intent(ShopItemsActivity.this, CartActivity.class);
            startActivity(intent);
        });

        // Home button functionality
        btnHome.setOnClickListener(view -> {
            Intent intent = new Intent(ShopItemsActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Account button functionality
        btnAccount.setOnClickListener(view -> {
            Intent intent = new Intent(ShopItemsActivity.this, UserInfoActivity.class);
            startActivity(intent);
        });
    }

    private void fetchItems() {
        // Query the Firebase database for items in the specified shop
        databaseReference.child("shops").child(shopName).child("items")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        loadingSpinner.setVisibility(View.GONE);  // Hide spinner once data is loaded

                        if (!snapshot.exists()) {
                            Toast.makeText(ShopItemsActivity.this, "No items found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Clear the previous item list
                        itemList.clear();

                        // Iterate through the items and add them to the layout
                        for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                            String itemName = itemSnapshot.child("name").getValue(String.class);
                            Integer itemPrice = itemSnapshot.child("price").getValue(Integer.class);

                            // Ensure itemPrice is not null before proceeding
                            if (itemPrice != null) {
                                Item item = new Item(itemName, itemPrice); // Create item object
                                itemList.add(item);  // Add item to the list
                                addItemToLayout(item);  // Add item to the layout
                            } else {
                                Log.e("ShopItemsActivity", "Item price is null for item: " + itemName);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        loadingSpinner.setVisibility(View.GONE);  // Hide spinner on error
                        Toast.makeText(ShopItemsActivity.this, "Failed to load items", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterItems(String query) {
        // Clear the layout
        shopItemsContainer.removeAllViews();

        // Check if query is empty
        if (query.isEmpty()) {
            // If the search is empty, display all items
            for (Item item : itemList) {
                addItemToLayout(item);
            }
        } else {
            // Display only items that match the search query
            for (Item item : itemList) {
                if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                    addItemToLayout(item);
                }
            }
        }
    }

    private void addItemToLayout(Item item) {
        // Inflate the item view layout using item_shop.xml
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_shop, shopItemsContainer, false);

        // Initialize item view components with the new IDs
        TextView itemNameTextView = itemView.findViewById(R.id.tv_item_name);
        TextView itemPriceTextView = itemView.findViewById(R.id.tv_item_price);
        Button addToCartButton = itemView.findViewById(R.id.btn_add_to_cart);

        // Set the item name and price
        itemNameTextView.setText(item.getName());
        itemPriceTextView.setText("₹" + item.getPrice());  // Formatting price as currency

        // Add click listener to the add to cart button
        addToCartButton.setOnClickListener(v -> {
            int quantity = 1;  // Default quantity when adding to cart
            addItemToCart(shopName, item.getName(), item.getPrice(), quantity);
        });

        // Add the item view to the container
        shopItemsContainer.addView(itemView);
    }

    private void addItemToCart(String shopName, String name, int price, int quantity) {
        // Assume CartActivity has a method addItem to handle the cart functionality
        CartActivity.addItem(shopName, name, price, quantity);
        Toast.makeText(this, name + " x" + quantity + " added to cart!", Toast.LENGTH_SHORT).show();
    }

    // Inner class to represent an Item
    private static class Item {
        private final String name;
        private final int price;

        public Item(String name, int price) {
            this.name = name;
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public int getPrice() {
            return price;
        }
    }
}
