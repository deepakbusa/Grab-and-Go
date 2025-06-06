package com.example.grabandgo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class previous_orders extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private LinearLayout ordersLayout;
    private DatabaseReference completedOrdersReference;
    private ImageView backImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.previous_orders);

        mAuth = FirebaseAuth.getInstance();
        completedOrdersReference = FirebaseDatabase.getInstance().getReference("CompletedOrders").child(mAuth.getCurrentUser().getUid());

        ordersLayout = findViewById(R.id.orders_layout);
        backImage = findViewById(R.id.back_image);

        fetchCompletedOrders();

        backImage.setOnClickListener(v -> finish()); // Go back to previous screen
    }

    private void fetchCompletedOrders() {
        completedOrdersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ordersLayout.removeAllViews();

                if (!dataSnapshot.exists()) {
                    Toast.makeText(previous_orders.this, "No previous orders found", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    String orderId = orderSnapshot.child("orderId").getValue(String.class);
                    String orderDate = orderSnapshot.child("orderDate").getValue(String.class);
                    String orderTime = orderSnapshot.child("orderTime").getValue(String.class);
                    String totalAmount = String.valueOf(orderSnapshot.child("totalOrderAmount").getValue(Integer.class));
                    String commission = String.valueOf(orderSnapshot.child("commission").getValue(Integer.class));
                    String totalQuantity = String.valueOf(orderSnapshot.child("totalQuantity").getValue(Integer.class));

                    View orderItemView = LayoutInflater.from(previous_orders.this).inflate(R.layout.order_display, ordersLayout, false);

                    TextView tvOrderId = orderItemView.findViewById(R.id.tv_order_id);
                    TextView tvOrderDate = orderItemView.findViewById(R.id.tv_order_date);
                    TextView tvOrderTime = orderItemView.findViewById(R.id.tv_order_time);
                    TextView tvTotalAmount = orderItemView.findViewById(R.id.tv_total_amount);
                    TextView tvCommission = orderItemView.findViewById(R.id.tv_commission);
                    TextView tvTotalQuantity = orderItemView.findViewById(R.id.tv_total_quantity);

                    tvOrderId.setText("Order ID: " + orderId);
                    tvOrderDate.setText("Order Date: " + orderDate);
                    tvOrderTime.setText("Order Time: " + orderTime);
                    tvTotalAmount.setText("Total Amount: ₹" + totalAmount);
                    tvCommission.setText("Commission: ₹" + commission);
                    tvTotalQuantity.setText("Total Quantity: " + totalQuantity);

                    // Remove any accept button if it exists
                    View acceptButton = orderItemView.findViewById(R.id.btn_accept);
                    if (acceptButton != null) {
                        ((LinearLayout) acceptButton.getParent()).removeView(acceptButton);
                    }

                    ordersLayout.addView(orderItemView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(previous_orders.this, "Error fetching data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
