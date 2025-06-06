package com.example.grabandgo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class order_display extends AppCompatActivity {

    private TextView tvOrderId, tvOrderDate, tvOrderTime, tvTotalAmount, tvCommission, tvTotalQuantity;
    private Button btnAccept;
    private String orderId = "Go2q3nDctQZQ75uuzpz2JEVVPWY2";  // Example order ID, replace dynamically as needed
    private String userId = "1731480295855";  // Example user ID, replace dynamically as needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_display);

        tvOrderId = findViewById(R.id.tv_order_id);
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvOrderTime = findViewById(R.id.tv_order_time);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvCommission = findViewById(R.id.tv_commission);
        tvTotalQuantity = findViewById(R.id.tv_total_quantity);
        btnAccept = findViewById(R.id.btn_accept);

        // Load data from Firebase for this order ID
        loadOrderDetails(orderId);

        // Set the button click listener to accept the order
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptOrder(orderId, userId);
            }
        });
    }

    private void loadOrderDetails(String orderId) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);

        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    tvOrderId.setText("Order ID: " + snapshot.child("orderId").getValue(String.class));
                    tvOrderDate.setText("Order Date: " + snapshot.child("orderDate").getValue(String.class));
                    tvOrderTime.setText("Order Time: " + snapshot.child("orderTime").getValue(String.class));
                    tvTotalAmount.setText("Total Amount: " + snapshot.child("totalAmount").getValue(String.class));
                    tvCommission.setText("Commission: " + snapshot.child("commission").getValue(String.class));
                    tvTotalQuantity.setText("Total Quantity: " + snapshot.child("totalQuantity").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(order_display.this, "Error loading order details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void acceptOrder(String orderId, String userId) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Orders").child(userId).child(orderId);
        DatabaseReference acceptedOrderRef = FirebaseDatabase.getInstance()
                .getReference("AcceptedOrders");

        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Copy the order details to "AcceptedOrders" node
                    acceptedOrderRef.setValue(snapshot.getValue()).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Delete the order from the "orders" node
                            orderRef.removeValue().addOnCompleteListener(removeTask -> {
                                if (removeTask.isSuccessful()) {
                                    Toast.makeText(order_display.this, "Order accepted successfully", Toast.LENGTH_SHORT).show();
                                    finish();  // Close the activity or navigate as needed
                                } else {
                                    Toast.makeText(order_display.this, "Failed to remove order", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(order_display.this, "Failed to accept order", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(order_display.this, "Order not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(order_display.this, "Error processing order", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
