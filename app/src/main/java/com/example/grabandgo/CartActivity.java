package com.example.grabandgo;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

import java.util.HashMap;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.graphics.Typeface;

public class CartActivity extends AppCompatActivity {

    private static final String TAG = "CartActivity";

    private static HashMap<String, Item> cartItems = new HashMap<>();
    private FirebaseAuth mAuth;
    private DatabaseReference cartDatabaseRef;

    private LinearLayout cartItemsContainer;
    private TextView tvCommission, tvTotalQuantity, tvOrderTotal, tvEmptyCart;
    private Button btnNext;
    private ImageButton btnBack, btnHome, btnProfile;

    public static final int COMMISSION_PER_ITEM = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            cartDatabaseRef = FirebaseDatabase.getInstance().getReference("CartItems").child(currentUser.getUid());
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Reload cart when returning from the confirmation screen
        // Reload cart when returning from the confirmation screen
        if (getIntent().getBooleanExtra("clearCart", false)) {
            cartItems.clear();  // Clear local cache
            loadCartItems();    // Reload the cart from Firebase (which is now empty)
        }



        cartItemsContainer = findViewById(R.id.cart_items_container);
        tvCommission = findViewById(R.id.tv_commission);
        tvTotalQuantity = findViewById(R.id.tv_total_quantity);
        tvOrderTotal = findViewById(R.id.tv_order_total);
        tvEmptyCart = findViewById(R.id.tv_empty_cart);
        btnNext = findViewById(R.id.btn_next);
        btnBack = findViewById(R.id.btn_back);
        btnHome = findViewById(R.id.btn_home);
        btnProfile = findViewById(R.id.btn_profile);
        ProgressBar progressBar = findViewById(R.id.progress_bar);


        btnNext.setEnabled(false);
        tvEmptyCart.setVisibility(View.GONE);

        loadCartItems();

        btnNext.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(CartActivity.this, "Cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            String cartItemsString = String.valueOf(getCartItemsString());
            new AlertDialog.Builder(CartActivity.this)
                    .setTitle("Confirm Your Order")
                    .setMessage(cartItemsString + "\nTotal Order Amount: ₹" + String.format("%.2f", getTotalOrderAmount()))
                    .setPositiveButton("Proceed", (dialog, which) -> {

                        // Navigate to PlaceOrderActivity
                        Intent intent = new Intent(CartActivity.this, PlaceOrderActivity.class);
                        intent.putExtra("totalOrderAmount", getTotalOrderAmount());

                        // Add a flag to clear the cart after placing the order
                        intent.putExtra("clearCart", true);

                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });



        btnBack.setOnClickListener(v -> finish());
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, ShopListActivity.class);
            startActivity(intent);
            finish();
        });
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, UserInfoActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // Define the Item class to hold the item's details, including quantity
    public static class Item {
        private String shopName;
        private String itemName;
        private double price;
        private int quantity;

        // Default constructor required for calls to DataSnapshot.getValue(Item.class)
        public Item() {
            // Firebase needs a default constructor
        }

        public Item(String shopName, String itemName, double price, int quantity) {
            this.shopName = shopName;
            this.itemName = itemName;
            this.price = price;
            this.quantity = quantity;
        }

        // Getters and Setters
        public String getShopName() {
            return shopName;
        }

        public void setShopName(String shopName) {
            this.shopName = shopName;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    public static void addItem(String shopName, String itemName, double price, int quantity) {
        // Check if the item already exists in the cart
        if (cartItems.containsKey(itemName)) {
            // If it is, update the quantity
            Item existingItem = cartItems.get(itemName);
            existingItem.setQuantity(existingItem.getQuantity() + quantity); // Add to existing quantity
        } else {
            // If it's not, create a new item
            Item newItem = new Item(shopName, itemName, price, quantity);
            cartItems.put(itemName, newItem); // Add to cart
        }
        saveCartItemToFirebase(cartItems.get(itemName)); // Save the updated item to Firebase
    }

    private static void saveCartItemToFirebase(Item item) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("CartItems")
                    .child(currentUser.getUid());
            cartRef.child(item.getItemName()).setValue(item); // Save item with name as key
        }
    }

    private void loadCartItems() {
        cartItemsContainer.removeAllViews();

        // Show the loading indicator before loading the data
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        cartDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItems.clear();

                if (!snapshot.exists()) {
                    tvEmptyCart.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyCart.setVisibility(View.GONE);
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        Item item = itemSnapshot.getValue(Item.class);
                        if (item != null) {
                            cartItems.put(item.getItemName(), item);
                            addCartItemView(item);
                        }
                    }
                }

                updateCommissionAndQuantity();
                calculateOrderTotal();

                // Hide the loading indicator once the data is loaded
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading cart items", error.toException());
                Toast.makeText(CartActivity.this, "Failed to load cart items", Toast.LENGTH_SHORT).show();

                // Hide the loading indicator in case of an error
                progressBar.setVisibility(View.GONE);
            }
        });
    }


    private void addCartItemView(Item item) {
        LayoutInflater inflater = LayoutInflater.from(this);
        // Inflate the new layout 'items_in_cart.xml' instead of 'item_cart.xml'
        View itemView = inflater.inflate(R.layout.items_in_cart, cartItemsContainer, false);

        TextView tvItemName = itemView.findViewById(R.id.item1_name);
        TextView tvItemPrice = itemView.findViewById(R.id.item1_price);
        TextView tvQuantity = itemView.findViewById(R.id.tv_quantity_value); // Corrected ID
        ImageButton btnIncrease = itemView.findViewById(R.id.btn_increase);
        ImageButton btnDecrease = itemView.findViewById(R.id.btn_decrease);

        tvItemName.setText(item.getShopName() + ": " + item.getItemName());
        tvItemPrice.setText("₹" + String.format("%.2f", item.getPrice()));
        tvQuantity.setText(String.valueOf(item.getQuantity())); // Corrected variable name

        btnIncrease.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            updateCartItemInFirebase(item);
            tvQuantity.setText(String.valueOf(item.getQuantity()));
            calculateOrderTotal();
            updateCommissionAndQuantity();
        });

        btnDecrease.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                updateCartItemInFirebase(item);
                tvQuantity.setText(String.valueOf(item.getQuantity()));
                calculateOrderTotal();
                updateCommissionAndQuantity();
            } else {
                showRemoveItemDialog(item);
            }
        });

        cartItemsContainer.addView(itemView);
    }

    private void showRemoveItemDialog(Item item) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Item")
                .setMessage("Do you want to remove " + item.getItemName() + " from the cart?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    removeCartItemFromFirebase(item);
                    loadCartItems(); // Reload cart items after removal
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void updateCartItemInFirebase(Item item) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("CartItems")
                    .child(currentUser.getUid())
                    .child(item.getItemName()); // Adjust to use name as a key
            itemRef.setValue(item);
        }
    }

    private void removeCartItemFromFirebase(Item item) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference itemRef = cartDatabaseRef.child(item.getItemName());
            itemRef.removeValue();
        }
    }

    private void calculateOrderTotal() {
        double totalOrderAmount = getTotalOrderAmount();
        tvOrderTotal.setText("Order Total: ₹" + String.format("%.2f", totalOrderAmount));
        btnNext.setEnabled(!cartItems.isEmpty());
    }

    private void updateCommissionAndQuantity() {
        int totalQuantity = getTotalQuantity();
        int commission = totalQuantity * COMMISSION_PER_ITEM;
        tvCommission.setText("Commission: ₹" + commission);
        tvTotalQuantity.setText("Total Quantity: " + totalQuantity);
    }

    static int getTotalQuantity() {
        int totalQuantity = 0;
        for (Item item : cartItems.values()) {
            totalQuantity += item.getQuantity();
        }
        return totalQuantity;
    }

    private double getTotalOrderAmount() {
        double totalAmount = 0.0;
        for (Item item : cartItems.values()) {
            totalAmount += item.getPrice() * item.getQuantity();
        }
        return totalAmount ;
    }

    static SpannableString getCartItemsString() {
        // Initial string with the header
        StringBuilder itemsString = new StringBuilder("Items in your cart:\n");

        // Append item details to the string builder
        for (Item item : cartItems.values()) {
            itemsString.append(item.getShopName())
                    .append(" - ")
                    .append(item.getItemName())
                    .append(" (Quantity: ")
                    .append(item.getQuantity())
                    .append(", Price: ₹")
                    .append(String.format("%.2f", item.getPrice()))
                    .append(")\n");
        }

        // Create a SpannableString from the built string
        SpannableString spannable = new SpannableString(itemsString.toString());

        // Apply bold style to the "Items in your cart:" part
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, "Items in your cart:".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannable;
    }
    // Method to retrieve cart items as a map
    public static HashMap<String, Item> getCartItems() {
        return cartItems;
    }

}
