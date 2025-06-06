package com.example.grabandgo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PlaceOrderActivity extends AppCompatActivity {

    private static final String TAG = "PlaceOrderActivity";

    // UI components
    private TextView tvTotalItems, tvCommission, tvOrderTotal, tvTotalAmount;
    private LinearLayout addressContainer;
    private Button btnConfirmOrder, btnAddAddress;
    private AppCompatImageButton btnBack, btnHome, btnUserAccount;

    // Firebase references
    private DatabaseReference addressRef;

    // Address selection variables
    private String selectedAddress;
    private Button selectedAddressButton;
    private ProgressBar loadingSpinner;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);  // Ensure correct layout

        // Initialize UI components
        initUI();

        // Firebase initialization
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        addressRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("addresses");

        // Load saved addresses from Firebase
        loadAddresses();

        // Update order totals from CartActivity
        updateTotals();

        // Handle button clicks
        setButtonListeners();
    }

    /**
     * Initialize all UI components
     */
    private void initUI() {
        tvTotalItems = findViewById(R.id.tv_total_items);
        tvCommission = findViewById(R.id.tv_commission);
        tvOrderTotal = findViewById(R.id.tv_order_total);
        tvTotalAmount = findViewById(R.id.tv_total);

        addressContainer = findViewById(R.id.saved_addresses_container);

        btnConfirmOrder = findViewById(R.id.btn_place_order);
        btnAddAddress = findViewById(R.id.btn_add_address);
        btnBack = findViewById(R.id.btn_back);
        btnHome = findViewById(R.id.btn_home);
        btnUserAccount = findViewById(R.id.btn_user_account);

        // Add loading spinner reference
        loadingSpinner = findViewById(R.id.loading_spinner);

        // Initially disable Confirm Order button
        btnConfirmOrder.setEnabled(false);
        btnConfirmOrder.setAlpha(0.5f);
    }

    /**
     * Set up click listeners for buttons
     */
    private void setButtonListeners() {
        // Add Address button click
        btnAddAddress.setOnClickListener(v -> {
            startActivity(new Intent(PlaceOrderActivity.this, AddAddressActivity.class));
        });

        // Confirm Order button click
        btnConfirmOrder.setOnClickListener(v -> confirmOrder());

        // Navigation buttons
        btnBack.setOnClickListener(v -> onBackPressed());

        btnHome.setOnClickListener(v -> {
            startActivity(new Intent(PlaceOrderActivity.this, ShopListActivity.class));
            finish();
        });

        btnUserAccount.setOnClickListener(v -> {
            startActivity(new Intent(PlaceOrderActivity.this, UserInfoActivity.class));
            finish();
        });
    }

    /**
     * Load saved addresses from Firebase
     */
    /**
     * Load saved addresses from Firebase
     */
    private void loadAddresses() {
        loadingSpinner.setVisibility(View.VISIBLE);
        addressContainer.setVisibility(View.GONE);  // Hide container while loading

        addressRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear container and reset the counter before adding addresses
                addressContainer.removeAllViews();
                addressCounter = 0;  // Reset the counter to start from 1

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AddressItem addressItem = snapshot.getValue(AddressItem.class);
                    if (addressItem != null) {
                        addAddressView(addressItem);
                    }
                }

                // Show container after loading
                loadingSpinner.setVisibility(View.GONE);
                addressContainer.setVisibility(View.VISIBLE);

                // Handle case where no addresses are found
                if (addressContainer.getChildCount() == 0) {
                    showNoAddressFoundMessage();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load addresses: ", error.toException());
                loadingSpinner.setVisibility(View.GONE);
                addressContainer.setVisibility(View.VISIBLE);
                Toast.makeText(PlaceOrderActivity.this, "Failed to load addresses. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int addressCounter = 0;
    /**
     * Dynamically create and add address views with numbering (Address 1, Address 2, ...)
     */
    private void addAddressView(AddressItem addressItem) {
        addressCounter++;  // Increment the counter by 1 for each address

        LayoutInflater inflater = LayoutInflater.from(this);
        View addressView = inflater.inflate(R.layout.item_address, addressContainer, false);

        // Address heading for numbering
        TextView tvAddressHeading = new TextView(this);
        tvAddressHeading.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        tvAddressHeading.setText("Address " + addressCounter);
        tvAddressHeading.setTextSize(16);
        tvAddressHeading.setTextColor(getResources().getColor(R.color.navy_blue));
        tvAddressHeading.setPadding(0, 8, 0, 8);
        tvAddressHeading.setTypeface(null, android.graphics.Typeface.BOLD);

        // Bind address data to views
        TextView tvMobileNumber = addressView.findViewById(R.id.tv_mobile_number);
        TextView tvRoomNumber = addressView.findViewById(R.id.tv_room_number);
        TextView tvBlock = addressView.findViewById(R.id.tv_block);
        TextView tvAddress = addressView.findViewById(R.id.tv_address);

        Button btnSelect = addressView.findViewById(R.id.btn_select);
        Button btnRemove = addressView.findViewById(R.id.btn_remove);

        // Set address details
        tvMobileNumber.setText("Mobile: " + addressItem.getMobileNumber());
        tvRoomNumber.setText("Room: " + addressItem.getRoomNumber());
        tvBlock.setText("Block: " + addressItem.getBlock());
        tvAddress.setText("Address: " + addressItem.getAddress());

        // Set tags to hold reference
        btnSelect.setTag(addressItem);

        // Select button click listener
        btnSelect.setOnClickListener(v -> selectAddress(btnSelect, addressItem));

        // Remove button click listener
        btnRemove.setOnClickListener(v -> removeAddress(addressItem));

        // Add the heading and address view
        addressContainer.addView(tvAddressHeading);
        addressContainer.addView(addressView);
    }


    /**
     * Handle address selection with drawable change
     */
    /**
     * Handle address selection with toggle functionality
     */
    private void selectAddress(Button btnSelect, AddressItem addressItem) {
        // If the clicked address is already selected, deselect it
        if (selectedAddressButton == btnSelect) {
            // Deselect current address
            selectedAddressButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_check_box_outline_blank, 0, 0, 0);
            selectedAddressButton = null;
            selectedAddress = null;

            // Disable "Confirm Order" button
            btnConfirmOrder.setEnabled(false);
            btnConfirmOrder.setAlpha(0.5f);
            Toast.makeText(this, "Address Deselected", Toast.LENGTH_SHORT).show();
        } else {
            // Deselect the previous address if any
            if (selectedAddressButton != null) {
                selectedAddressButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_check_box_outline_blank, 0, 0, 0);
            }

            // Select new address
            selectedAddressButton = btnSelect;
            selectedAddress = addressItem.getAddress();

            // Change drawable to checked icon for the new selection
            selectedAddressButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_check_box_, 0, 0, 0);

            // Enable "Confirm Order" button
            btnConfirmOrder.setEnabled(true);
            btnConfirmOrder.setAlpha(1.0f);
            Toast.makeText(this, "Address Selected: " + selectedAddress, Toast.LENGTH_SHORT).show();
        }

        // Now, place the onClickListener outside to prevent placing the order when toggling
        btnSelect.setOnClickListener(v -> {
            // If this is the selected address and user clicks again, just toggle the checkbox
            if (selectedAddressButton == btnSelect) {
                // If already selected, toggle the checkbox back to empty state and disable the "Confirm Order" button
                selectedAddressButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_check_box_outline_blank, 0, 0, 0);
                selectedAddressButton = null;
                selectedAddress = null;

                btnConfirmOrder.setEnabled(false);
                btnConfirmOrder.setAlpha(0.5f);
                Toast.makeText(PlaceOrderActivity.this, "Address Deselected", Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, update the selected address and proceed as usual
                selectedAddressButton = btnSelect;
                selectedAddress = addressItem.getAddress();

                // Change drawable to checked icon
                selectedAddressButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_check_box_, 0, 0, 0);

                // Enable "Confirm Order" button
                btnConfirmOrder.setEnabled(true);
                btnConfirmOrder.setAlpha(1.0f);
                Toast.makeText(PlaceOrderActivity.this, "Address Selected: " + selectedAddress, Toast.LENGTH_SHORT).show();
            }
        });
    }



    /**
     * Remove address from Firebase
     */
    /**
     * Remove address from Firebase
     */
    private void removeAddress(AddressItem addressItem) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Address")
                .setMessage("Are you sure you want to remove this address?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    addressRef.child(addressItem.getId()).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Address removed successfully.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to remove address.", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Display message when no addresses are found
     */
    private void showNoAddressFoundMessage() {
        TextView noAddressMessage = new TextView(this);
        noAddressMessage.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        noAddressMessage.setText("No Address Found");
        noAddressMessage.setTextSize(16);
        noAddressMessage.setTextColor(getResources().getColor(R.color.navy_blue));
        noAddressMessage.setGravity(View.TEXT_ALIGNMENT_CENTER);
        noAddressMessage.setPadding(16, 16, 16, 16);

        addressContainer.addView(noAddressMessage);
    }



    /**
     * Confirm the order and proceed to the confirmation activity
     */
    /**
     * Confirm the order and proceed to the confirmation activity
     */
    private void confirmOrder() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Your Order")
                .setMessage("Do you want to confirm your order?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    if (selectedAddressButton != null) {
                        AddressItem addressItem = (AddressItem) selectedAddressButton.getTag();

                        // Intent to OrderConfirmationActivity with clearCart flag
                        Intent confirmationIntent = new Intent(PlaceOrderActivity.this, OrderConfirmationActivity.class);

                        // Pass full address details
                        confirmationIntent.putExtra("selectedAddress", addressItem.getAddress());
                        confirmationIntent.putExtra("mobileNumber", addressItem.getMobileNumber());
                        confirmationIntent.putExtra("roomNumber", addressItem.getRoomNumber());
                        confirmationIntent.putExtra("block", addressItem.getBlock());

                        // Pass total amount
                        double totalOrderAmount = getIntent().getDoubleExtra("totalOrderAmount", 0.0);
                        confirmationIntent.putExtra("totalOrderAmount", totalOrderAmount);

                        // Add flag to clear cart after placing order
                        confirmationIntent.putExtra("clearCart", true);

                        startActivity(confirmationIntent);
                        finish();
                    } else {
                        Toast.makeText(this, "Please select an address", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Discard", null)
                .show();
    }


    /**
     * Update totals dynamically
     */
    private void updateTotals() {
        double totalOrderAmount = getIntent().getDoubleExtra("totalOrderAmount", 0.0);
        int totalQuantity = CartActivity.getTotalQuantity();
        double commission = totalQuantity * CartActivity.COMMISSION_PER_ITEM;

        tvTotalItems.setText("Total items: " + totalQuantity);
        tvCommission.setText("Commission: ₹" + commission);
        tvOrderTotal.setText("Order Total: ₹" + String.format("%.2f", totalOrderAmount));
        tvTotalAmount.setText("Total Amount: ₹" + String.format("%.2f", totalOrderAmount + commission));
    }
}
