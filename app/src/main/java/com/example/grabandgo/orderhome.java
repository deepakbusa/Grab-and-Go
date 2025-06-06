package com.example.grabandgo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class orderhome extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ImageView BackImage;
    private EditText searchBar;
    private LinearLayout ordersLayout;
    private DatabaseReference ordersReference;
    private DatabaseReference acceptedOrdersReference;
    private List<View> allOrderViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orderhome);
        searchBar = findViewById(R.id.search_bar);
        searchBar.setTextColor(getResources().getColor(android.R.color.black));

        mAuth = FirebaseAuth.getInstance();
        ordersReference = FirebaseDatabase.getInstance().getReference("Orders");
        acceptedOrdersReference = FirebaseDatabase.getInstance().getReference("AcceptedOrders");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        BackImage = findViewById(R.id.back_image);
        ordersLayout = findViewById(R.id.orders_layout);

        fetchOrderData();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterOrders(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.logout_image).setOnClickListener(v -> logout());
        findViewById(R.id.user_icon).setOnClickListener(v -> goToUserOrder());
    }


    private void fetchOrderData() {
        ordersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ordersLayout.removeAllViews();
                allOrderViews.clear();

                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot orderDetailSnapshot : orderSnapshot.getChildren()) {
                        String orderId = orderDetailSnapshot.child("orderId").getValue(String.class);
                        String orderDate = orderDetailSnapshot.child("orderDate").getValue(String.class);
                        String orderTime = orderDetailSnapshot.child("orderTime").getValue(String.class);
                        String totalAmount = String.valueOf(orderDetailSnapshot.child("totalOrderAmount").getValue(Integer.class));
                        String commission = String.valueOf(orderDetailSnapshot.child("commission").getValue(Integer.class));
                        String totalQuantity = String.valueOf(orderDetailSnapshot.child("totalQuantity").getValue(Integer.class));

                        View orderItemView = LayoutInflater.from(orderhome.this).inflate(R.layout.order_display, ordersLayout, false);

                        TextView tvOrderId = orderItemView.findViewById(R.id.tv_order_id);
                        TextView tvOrderDate = orderItemView.findViewById(R.id.tv_order_date);
                        TextView tvOrderTime = orderItemView.findViewById(R.id.tv_order_time);
                        TextView tvTotalAmount = orderItemView.findViewById(R.id.tv_total_amount);
                        TextView tvCommission = orderItemView.findViewById(R.id.tv_commission);
                        TextView tvTotalQuantity = orderItemView.findViewById(R.id.tv_total_quantity);
                        Button btnAccept = orderItemView.findViewById(R.id.btn_accept);

                        tvOrderId.setText("Order ID: " + orderId);
                        tvOrderDate.setText("Order Date: " + orderDate);
                        tvOrderTime.setText("Order Time: " + orderTime);
                        tvTotalAmount.setText("Total Amount: ₹" + totalAmount);
                        tvCommission.setText("Commission: ₹" + commission);
                        tvTotalQuantity.setText("Total Quantity: " + totalQuantity);

                        btnAccept.setOnClickListener(v -> showConfirmationDialog(orderSnapshot.getKey(), orderDetailSnapshot));

                        ordersLayout.addView(orderItemView);
                        allOrderViews.add(orderItemView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(orderhome.this, "Error fetching data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showConfirmationDialog(String parentKey, DataSnapshot orderDetailSnapshot) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("After accepting, no more cancellations will be allowed. The order must be delivered within 3 hours. Failure to deliver may result in a fine. Are you sure you want to proceed?");

        builder.setPositiveButton("Yes, Accept", (dialog, which) -> {
            moveToAcceptedOrders(parentKey, orderDetailSnapshot);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void moveToAcceptedOrders(String parentKey, DataSnapshot orderDetailSnapshot) {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userAcceptedOrders = acceptedOrdersReference.child(userId).push();

        userAcceptedOrders.setValue(orderDetailSnapshot.getValue()).addOnSuccessListener(aVoid -> {
            // Data successfully moved to AcceptedOrders, now remove from Orders
            ordersReference.child(parentKey).child(orderDetailSnapshot.getKey()).removeValue().addOnSuccessListener(aVoid2 -> {
                Toast.makeText(orderhome.this, "Order Accepted and Moved to Accepted Orders", Toast.LENGTH_SHORT).show();

                // After successful removal, navigate to the CurrentOrdersActivity
                Intent intent = new Intent(orderhome.this, current_orders.class);
                startActivity(intent);
                finish(); // Optional, to prevent returning to previous activity
            }).addOnFailureListener(e -> {
                Toast.makeText(orderhome.this, "Failed to Remove Order from Orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(orderhome.this, "Failed to Accept Order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void logout() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            mGoogleSignInClient.revokeAccess().addOnCompleteListener(this, task1 -> {
                startActivity(new Intent(orderhome.this, orderlogin.class));
                finish();
            });
        });
    }
    private void filterOrders(String query) {
        query = query.toLowerCase().trim();

        for (View orderView : allOrderViews) {
            TextView tvOrderId = orderView.findViewById(R.id.tv_order_id);
            TextView tvOrderDate = orderView.findViewById(R.id.tv_order_date);
            TextView tvOrderTime = orderView.findViewById(R.id.tv_order_time);
            TextView tvTotalAmount = orderView.findViewById(R.id.tv_total_amount);

            String orderId = tvOrderId.getText().toString().toLowerCase();
            String orderDate = tvOrderDate.getText().toString().toLowerCase();
            String orderTime = tvOrderTime.getText().toString().toLowerCase();
            String totalAmount = tvTotalAmount.getText().toString().toLowerCase();

            if (orderId.contains(query) || orderDate.contains(query) || orderTime.contains(query) || totalAmount.contains(query)) {
                orderView.setVisibility(View.VISIBLE);
            } else {
                orderView.setVisibility(View.GONE);
            }
        }
    }


    private void goToUserOrder() {
        startActivity(new Intent(orderhome.this, orderuser.class));
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}