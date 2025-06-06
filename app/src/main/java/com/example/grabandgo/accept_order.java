package com.example.grabandgo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class accept_order extends AppCompatActivity {

    private TextView tvOrderId, tvOrderStatus, tvItems, tvCommission, tvTotalAmount,
            tvAddress, tvMobileNumber, tvRoomNumber, tvBlock, tvOrderDate, tvOrderTime;
    private Button btnBack, btnCompleteOrder;
    private DatabaseReference ordersRef;
    private String userId, acceptedOrderKey;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accept_order);

        // Initialize UI components
        tvOrderId = findViewById(R.id.tv_order_id);
        tvOrderStatus = findViewById(R.id.tv_order_status);
        tvItems = findViewById(R.id.tv_items);
        tvCommission = findViewById(R.id.tv_commission);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvAddress = findViewById(R.id.tv_address);
        tvMobileNumber = findViewById(R.id.tv_mobile_number);
        tvRoomNumber = findViewById(R.id.tv_room_number);
        tvBlock = findViewById(R.id.tv_block);
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvOrderTime = findViewById(R.id.tv_order_time);
        btnBack = findViewById(R.id.btn_go_home);
        btnCompleteOrder = findViewById(R.id.btn_complete_order);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        acceptedOrderKey = getIntent().getStringExtra("acceptedOrderKey");
        ordersRef = FirebaseDatabase.getInstance().getReference("AcceptedOrders").child(userId).child(acceptedOrderKey);

        fetchOrderDetails();

        // Back button functionality
        btnBack.setOnClickListener(view -> startActivity(new Intent(accept_order.this, current_orders.class)));

        // Complete Order with OTP Verification
        btnCompleteOrder.setOnClickListener(view -> sendOtp());
    }

    private void fetchOrderDetails() {
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    tvOrderId.setText("Order ID: " + snapshot.child("orderId").getValue(String.class));
                    tvAddress.setText("Address: " + snapshot.child("address").getValue(String.class));
                    tvBlock.setText("Block: " + snapshot.child("block").getValue(String.class));
                    tvCommission.setText("Commission: " + snapshot.child("commission").getValue(Long.class));
                    tvTotalAmount.setText("Total Amount: " + snapshot.child("totalOrderAmount").getValue(Long.class));
                    tvMobileNumber.setText("Mobile Number: " + snapshot.child("mobileNumber").getValue(String.class));
                    tvOrderDate.setText("Order Date: " + snapshot.child("orderDate").getValue(String.class));
                    tvOrderTime.setText("Order Time: " + snapshot.child("orderTime").getValue(String.class));
                    tvRoomNumber.setText("Room Number: " + snapshot.child("roomNumber").getValue(String.class));

                    StringBuilder itemsText = new StringBuilder();
                    for (DataSnapshot itemSnapshot : snapshot.child("items").getChildren()) {
                        itemsText.append("Item: ").append(itemSnapshot.child("name").getValue(String.class)).append("\n")
                                .append("Price: ").append(itemSnapshot.child("price").getValue(Long.class)).append("\n")
                                .append("Quantity: ").append(itemSnapshot.child("quantity").getValue(Long.class)).append("\n")
                                .append("Shop: ").append(itemSnapshot.child("shopName").getValue(String.class)).append("\n\n");
                    }
                    tvItems.setText(itemsText.toString());
                } else {
                    Toast.makeText(accept_order.this, "Order not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(accept_order.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendOtp() {
        String phoneNumber = tvMobileNumber.getText().toString();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        completeOrder();
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(accept_order.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = s;
                        showOtpDialog();
                    }
                });
    }

    private void showOtpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_otp, null);
        EditText etOtp = dialogView.findViewById(R.id.et_otp);
        builder.setView(dialogView);
        builder.setPositiveButton("Submit", (dialog, which) -> {
            String otp = etOtp.getText().toString();
            verifyOtp(otp);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void verifyOtp(String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        completeOrder();
    }

    private void completeOrder() {
        DatabaseReference completedRef = FirebaseDatabase.getInstance().getReference("CompletedOrders").child(userId).child(acceptedOrderKey);
        DatabaseReference orderUserRef = FirebaseDatabase.getInstance().getReference("OrderUsers").child(userId).child("incomeEarned");

        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Move order to CompletedOrders
                    completedRef.setValue(snapshot.getValue(), (error, ref) -> {
                        if (error == null) {
                            // Remove from AcceptedOrders
                            ordersRef.removeValue();

                            // Get the commission from the order
                            Long commission = snapshot.child("commission").getValue(Long.class);

                            // Update incomeEarned
                            orderUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Long currentIncome = dataSnapshot.exists() ? dataSnapshot.getValue(Long.class) : 0L;
                                    orderUserRef.setValue(currentIncome + commission);

                                    Toast.makeText(accept_order.this, "Order Completed and Commission Added!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(accept_order.this, current_orders.class));
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(accept_order.this, "Error Updating Income", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(accept_order.this, "Error Completing Order", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(accept_order.this, "Error Completing Order", Toast.LENGTH_SHORT).show();
            }
        });
    }
}