package com.example.grabandgo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OrderHistoryActivity extends AppCompatActivity {

    private LinearLayout orderItemsLayout;
    private ScrollView scrollViewOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        orderItemsLayout = findViewById(R.id.linear_layout_order_items);
        scrollViewOrders = findViewById(R.id.scroll_view_orders);
        ImageView imgBack = findViewById(R.id.img_back);
        ImageView imgHome = findViewById(R.id.img_home);
        ImageView imgUser = findViewById(R.id.img_user);

        imgBack.setOnClickListener(v -> finish()); // Go back to the previous activity

        imgHome.setOnClickListener(v -> {
            Intent intent = new Intent(OrderHistoryActivity.this, ShopListActivity.class);
            startActivity(intent);
        }); // Navigate to ShopListActivity

        imgUser.setOnClickListener(v -> {
            Intent intent = new Intent(OrderHistoryActivity.this, UserInfoActivity.class);
            startActivity(intent);
        }); // Navigate to UserInfoActivity

        loadOrderHistory();
    }

    private void loadOrderHistory() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Orders").child(currentUser.getUid());

            ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    orderItemsLayout.removeAllViews();

                    if (!dataSnapshot.exists()) {
                        Toast.makeText(OrderHistoryActivity.this, "No orders found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<OrderDetails> orderList = new ArrayList<>();

                    for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                        // Fetch the order details first (excluding items)
                        OrderDetails orderDetails = new OrderDetails();

                        orderDetails.orderId = orderSnapshot.child("orderId").getValue(String.class);
                        orderDetails.address = orderSnapshot.child("address").getValue(String.class);
                        orderDetails.block = orderSnapshot.child("block").getValue(String.class);
                        orderDetails.commission = orderSnapshot.child("commission").getValue(Double.class);
                        orderDetails.mobileNumber = orderSnapshot.child("mobileNumber").getValue(String.class);
                        orderDetails.orderDate = orderSnapshot.child("orderDate").getValue(String.class);
                        orderDetails.orderTime = orderSnapshot.child("orderTime").getValue(String.class);
                        orderDetails.totalOrderAmount = orderSnapshot.child("totalOrderAmount").getValue(Double.class);
                        orderDetails.totalQuantity = orderSnapshot.child("totalQuantity").getValue(Integer.class);

                        // Fetch the items node separately
                        DataSnapshot itemsSnapshot = orderSnapshot.child("Items");  // Use "Items" node as in the database
                        Map<String, OrderItem> itemsMap = new java.util.HashMap<>();

                        for (DataSnapshot itemSnap : itemsSnapshot.getChildren()) {
                            OrderItem item = itemSnap.getValue(OrderItem.class);
                            if (item != null) {
                                itemsMap.put(itemSnap.getKey(), item);
                            }
                        }

                        // Add the items to the order
                        orderDetails.setItems(itemsMap);
                        orderList.add(orderDetails);
                    }

                    // Sort orders by date (latest first)
                    Collections.sort(orderList, (o1, o2) -> o2.orderDate.compareTo(o1.orderDate));

                    for (OrderDetails order : orderList) {
                        View orderView = createOrderView(order);
                        orderItemsLayout.addView(orderView);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(OrderHistoryActivity.this, "Failed to load orders: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
        }
    }




    private View createOrderView(OrderDetails orderDetails) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.item_order_history, orderItemsLayout, false);


        // Initialize TextViews
        TextView tvOrderId = view.findViewById(R.id.tv_order_id);
        TextView tvAddress = view.findViewById(R.id.tv_address);
        TextView tvMobileNumber = view.findViewById(R.id.tv_mobile_number);
        TextView tvRoomNumber = view.findViewById(R.id.tv_room_number);
        TextView tvBlock = view.findViewById(R.id.tv_block);
        TextView tvTotalAmount = view.findViewById(R.id.tv_total_amount);
        TextView tvCommission = view.findViewById(R.id.tv_commission);
        TextView tvTotalQuantity = view.findViewById(R.id.tv_total_quantity);
        TextView tvOrderDate = view.findViewById(R.id.tv_order_date);
        TextView tvOrderTime = view.findViewById(R.id.tv_order_time);

        // Reference the LinearLayout for items
        LinearLayout itemListLayout = view.findViewById(R.id.linear_layout_item_list);

        // Set order details
        tvOrderId.setText("Order ID: #" + orderDetails.orderId);
        tvAddress.setText("Address: " + orderDetails.address);
        tvMobileNumber.setText("Mobile Number: " + orderDetails.mobileNumber);
        tvRoomNumber.setText("Room Number: " + orderDetails.roomNumber);
        tvBlock.setText("Block: " + orderDetails.block);
        tvTotalAmount.setText("Total Amount: ₹" + String.format("%.2f", orderDetails.totalOrderAmount));
        tvCommission.setText("Commission: ₹" + orderDetails.commission);
        tvTotalQuantity.setText("Total Quantity: " + orderDetails.totalQuantity);
        tvOrderDate.setText("Order Date: " + orderDetails.orderDate);
        tvOrderTime.setText("Order Time: " + orderDetails.orderTime);

        // Display the items inside the order history
        itemListLayout.removeAllViews();

        if (orderDetails.getItems() != null && !orderDetails.getItems().isEmpty()) {
            for (Map.Entry<String, OrderItem> entry : orderDetails.getItems().entrySet()) {
                OrderItem item = entry.getValue();

                // Inflate and populate item_order_detail.xml
                View itemView = LayoutInflater.from(this).inflate(R.layout.item_order_detail, itemListLayout, false);


                TextView tvShopName = itemView.findViewById(R.id.tv_shop_name);
                TextView tvItemName = itemView.findViewById(R.id.tv_item_name);
                TextView tvItemPrice = itemView.findViewById(R.id.tv_item_price);
                TextView tvItemQuantity = itemView.findViewById(R.id.tv_item_quantity);

                // Set values
                tvShopName.setText("Shop: " + item.getShopName());
                tvItemName.setText("Item: " + item.getItemName());
                tvItemPrice.setText("Price: ₹" + String.format("%.2f", item.getPrice()));
                tvItemQuantity.setText("Quantity: " + item.getQuantity());

                // Add the item view under the order
                itemListLayout.addView(itemView);
            }
        }

        return view;
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
        public String orderDate;
        public String orderTime;
        public Map<String, OrderItem> items;

        public OrderDetails() {
            // Default constructor required for Firebase
        }

        public Map<String, OrderItem> getItems() {
            return items;
        }

        public void setItems(Map<String, OrderItem> items) {
            this.items = items;
        }
    }

    static class OrderItem {
        public String itemName;       // Change from name → itemName
        public double price;
        public int quantity;
        public String shopName;

        public OrderItem() {
            // Default constructor required for Firebase
        }

        public String getItemName() {     // Use itemName getter
            return itemName;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }

        public String getShopName() {
            return shopName;
        }
    }


}
