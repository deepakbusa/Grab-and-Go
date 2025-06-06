package com.example.grabandgo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.graphics.Typeface;
import android.widget.ImageButton;


public class OrderConfirmationActivity extends AppCompatActivity {

    private TextView tvOrderId, tvOrderStatus, tvItems, tvCommission, tvTotalAmount, tvAddress, tvMobileNumber, tvRoomNumber, tvBlock, tvOrderDate, tvOrderTime;

    private ImageButton btnGoHome;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation); // Ensure this layout exists

        // Initialize TextViews
        tvOrderId = findViewById(R.id.tv_order_id);
        tvOrderStatus = findViewById(R.id.tv_order_status);
        tvItems = findViewById(R.id.tv_items);
        tvCommission = findViewById(R.id.tv_commission);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvAddress = findViewById(R.id.tv_address);
        tvMobileNumber = findViewById(R.id.tv_mobile_number);
        tvRoomNumber = findViewById(R.id.tv_room_number);
        tvBlock = findViewById(R.id.tv_block);
        tvOrderDate = findViewById(R.id.tv_order_date); // Order date TextView
        tvOrderTime = findViewById(R.id.tv_order_time); // Order time TextView
        btnGoHome = findViewById(R.id.btn_go_home);  // For regular button
        ImageButton btnHome = findViewById(R.id.btn_home);  // For ImageButton
        ImageButton btnProfile = findViewById(R.id.btn_profile);

        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                Intent profileIntent = new Intent(OrderConfirmationActivity.this, UserInfoActivity.class);
                startActivity(profileIntent);
            });
        }

// Null check before setting listeners
        if (btnGoHome != null) {
            btnGoHome.setOnClickListener(v -> {
                Intent homeIntent = new Intent(OrderConfirmationActivity.this, ShopListActivity.class);
                startActivity(homeIntent);
                finish();
            });
        }

        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                Intent homeIntent = new Intent(OrderConfirmationActivity.this, ShopListActivity.class);
                startActivity(homeIntent);
                finish();
            });
        }



        // Check for null TextViews for debugging
        checkTextViewInitialization();

        // Get intent data
        Intent intent = getIntent();
        String selectedAddress = intent.getStringExtra("selectedAddress");
        String mobileNumber = intent.getStringExtra("mobileNumber");
        String roomNumber = intent.getStringExtra("roomNumber");
        String block = intent.getStringExtra("block");

// Display the address details
        tvAddress.setText(getBoldSpannable("Address: ", selectedAddress));
        tvMobileNumber.setText(getBoldSpannable("Mobile Number: ", mobileNumber));
        tvRoomNumber.setText(getBoldSpannable("Room Number: ", roomNumber));
        tvBlock.setText(getBoldSpannable("Block: ", block));


        double totalOrderAmount = intent.getDoubleExtra("totalOrderAmount", 0.0);
        int totalQuantity = CartActivity.getTotalQuantity(); // Fetch total items from cart
        double commission = totalQuantity * CartActivity.COMMISSION_PER_ITEM;

        // Generate a unique order ID
        String orderId = generateOrderId();

        // Get current date and time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        String orderDate = dateFormat.format(calendar.getTime());
        String orderTime = timeFormat.format(calendar.getTime());

        // Set order confirmation details with bold labels
        tvOrderId.setText(getBoldSpannable("Order ID: ", "#" + orderId));
        tvOrderStatus.setText("Your Order is Successfully Placed.");
        tvItems.setText(CartActivity.getCartItemsString());
        tvCommission.setText(getBoldSpannable("Total Commission: ", "₹" + commission));
        tvTotalAmount.setText(getBoldSpannable("Total Amount: ", "₹" + String.format("%.2f", totalOrderAmount + commission)));
        tvAddress.setText(getBoldSpannable("Address: ", selectedAddress));
        tvMobileNumber.setText(getBoldSpannable("Mobile Number: ", mobileNumber));
        tvRoomNumber.setText(getBoldSpannable("Room Number: ", roomNumber));
        tvBlock.setText(getBoldSpannable("Block: ", block));
        tvOrderDate.setText(getBoldSpannable("Order Date: ", orderDate));
        tvOrderTime.setText(getBoldSpannable("Order Time: ", orderTime));


        // Go Home button action
        btnGoHome.setOnClickListener(v -> {
            Intent homeIntent = new Intent(OrderConfirmationActivity.this, ShopListActivity.class);
            startActivity(homeIntent);
            finish(); // Close the confirmation activity
        });

        // Save order details to Firebase
        saveOrderDetailsToFirebase(orderId, selectedAddress, mobileNumber, roomNumber, block, totalOrderAmount, commission, totalQuantity, orderDate, orderTime);
    }
    private SpannableString getBoldSpannable(String label, String value) {
        SpannableString spannable = new SpannableString(label + value);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }


    private void checkTextViewInitialization() {
        if (tvOrderId == null) Log.e("OrderConfirmationActivity", "tvOrderId is null");
        if (tvOrderStatus == null) Log.e("OrderConfirmationActivity", "tvOrderStatus is null");
        if (tvItems == null) Log.e("OrderConfirmationActivity", "tvItems is null");
        if (tvCommission == null) Log.e("OrderConfirmationActivity", "tvCommission is null");
        if (tvTotalAmount == null) Log.e("OrderConfirmationActivity", "tvTotalAmount is null");
        if (tvAddress == null) Log.e("OrderConfirmationActivity", "tvAddress is null");
        if (tvMobileNumber == null) Log.e("OrderConfirmationActivity", "tvMobileNumber is null");
        if (tvRoomNumber == null) Log.e("OrderConfirmationActivity", "tvRoomNumber is null");
        if (tvBlock == null) Log.e("OrderConfirmationActivity", "tvBlock is null");
        if (tvOrderDate == null) Log.e("OrderConfirmationActivity", "tvOrderDate is null");
        if (tvOrderTime == null) Log.e("OrderConfirmationActivity", "tvOrderTime is null");
        if (btnGoHome == null) Log.e("OrderConfirmationActivity", "btnGoHome is null");
    }

    private String generateOrderId() {
        return String.valueOf(System.currentTimeMillis());
    }

    private void saveOrderDetailsToFirebase(String orderId, String address, String mobileNumber, String roomNumber, String block, double totalOrderAmount, double commission, int totalQuantity, String orderDate, String orderTime) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Orders").child(currentUser.getUid());

            // Create order details object
            OrderDetails orderDetails = new OrderDetails(orderId, address, mobileNumber, roomNumber, block, totalOrderAmount, commission, totalQuantity, orderDate, orderTime);

            // Save the basic order details first
            orderRef.child(orderId).setValue(orderDetails).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Now add items under the "Items" node inside the order
                    DatabaseReference itemsRef = orderRef.child(orderId).child("Items");

                    // Iterate through the cart items and add them to the "Items" node
                    for (CartActivity.Item item : CartActivity.getCartItems().values()) {
                        String itemId = itemsRef.push().getKey();  // Generate a unique ID for each item

                        if (itemId != null) {
                            itemsRef.child(itemId).setValue(new OrderItem(
                                    item.getShopName(),
                                    item.getItemName(),
                                    item.getQuantity(),
                                    item.getPrice()
                            ));
                        }
                    }

                    // Clear the cart in Firebase after successful order placement
                    clearCartInFirebase();

                    Toast.makeText(OrderConfirmationActivity.this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(OrderConfirmationActivity.this, "Failed to save order details.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Method to clear the cart in Firebase
    private void clearCartInFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference cartRef = FirebaseDatabase.getInstance()
                    .getReference("CartItems")
                    .child(currentUser.getUid());

            // Remove all cart items
            cartRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Cart cleared successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to clear cart.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    static class OrderDetails {
        public String orderId;
        public String address;
        public String mobileNumber;
        public String roomNumber;
        public String block;
        public double totalOrderAmount;
        public double commission;
        public int totalQuantity;
        public String orderDate; // Added for order date
        public String orderTime; // Added for order time

        public OrderDetails() {
            // Default constructor required for calls to DataSnapshot.getValue(OrderDetails.class)
        }

        public OrderDetails(String orderId, String address, String mobileNumber, String roomNumber, String block, double totalOrderAmount, double commission, int totalQuantity, String orderDate, String orderTime) {
            this.orderId = orderId;
            this.address = address;
            this.mobileNumber = mobileNumber;
            this.roomNumber = roomNumber;
            this.block = block;
            this.totalOrderAmount = totalOrderAmount;
            this.commission = commission;
            this.totalQuantity = totalQuantity;
            this.orderDate = orderDate; // Set order date
            this.orderTime = orderTime; // Set order time
        }
    }
    // Class to represent individual items in the order
    static class OrderItem {
        public String shopName;
        public String itemName;
        public int quantity;
        public double price;

        // Default constructor required for Firebase
        public OrderItem() {
        }

        public OrderItem(String shopName, String itemName, int quantity, double price) {
            this.shopName = shopName;
            this.itemName = itemName;
            this.quantity = quantity;
            this.price = price;
        }
    }

}