package com.example.grabandgo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class current_orders extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private LinearLayout ordersLayout;
    private DatabaseReference acceptedOrdersReference;
    private ImageView backImage, logoutImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_orders);

        mAuth = FirebaseAuth.getInstance();
        acceptedOrdersReference = FirebaseDatabase.getInstance().getReference("AcceptedOrders").child(mAuth.getCurrentUser().getUid());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        ordersLayout = findViewById(R.id.orders_layout);
        backImage = findViewById(R.id.back_image);
        logoutImage = findViewById(R.id.logout_image);

        fetchAcceptedOrders();

        backImage.setOnClickListener(v -> startActivity(new Intent(current_orders.this, orderuser.class)));
        logoutImage.setOnClickListener(v -> signOut());
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (acceptedOrdersReference != null) {
            acceptedOrdersReference.removeEventListener((ValueEventListener) this);
        }
    }


    private void fetchAcceptedOrders() {
        acceptedOrdersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ordersLayout.removeAllViews();

                if (!dataSnapshot.exists()) {
                    Toast.makeText(current_orders.this, "No accepted orders found", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    String acceptedOrderKey = orderSnapshot.getKey();
                    String orderId = orderSnapshot.child("orderId").getValue(String.class);
                    String orderDate = orderSnapshot.child("orderDate").getValue(String.class);
                    String orderTime = orderSnapshot.child("orderTime").getValue(String.class);
                    String totalAmount = String.valueOf(orderSnapshot.child("totalOrderAmount").getValue(Integer.class));
                    String commission = String.valueOf(orderSnapshot.child("commission").getValue(Integer.class));
                    String totalQuantity = String.valueOf(orderSnapshot.child("totalQuantity").getValue(Integer.class));

                    View orderItemView = LayoutInflater.from(current_orders.this).inflate(R.layout.order_display, ordersLayout, false);

                    TextView tvOrderId = orderItemView.findViewById(R.id.tv_order_id);
                    TextView tvOrderDate = orderItemView.findViewById(R.id.tv_order_date);
                    TextView tvOrderTime = orderItemView.findViewById(R.id.tv_order_time);
                    TextView tvTotalAmount = orderItemView.findViewById(R.id.tv_total_amount);
                    TextView tvCommission = orderItemView.findViewById(R.id.tv_commission);
                    TextView tvTotalQuantity = orderItemView.findViewById(R.id.tv_total_quantity);
                    Button btnOpen = orderItemView.findViewById(R.id.btn_accept);

                    tvOrderId.setText("Order ID: " + orderId);
                    tvOrderDate.setText("Order Date: " + orderDate);
                    tvOrderTime.setText("Order Time: " + orderTime);
                    tvTotalAmount.setText("Total Amount: ₹" + totalAmount);
                    tvCommission.setText("Commission: ₹" + commission);
                    tvTotalQuantity.setText("Total Quantity: " + totalQuantity);
                    btnOpen.setText("Open");

                    btnOpen.setOnClickListener(v -> {
                        Intent intent = new Intent(current_orders.this, accept_order.class);
                        intent.putExtra("orderId", orderId);
                        intent.putExtra("acceptedOrderKey", acceptedOrderKey);
                        startActivity(intent);
                    });

                    ordersLayout.addView(orderItemView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(current_orders.this, "Error fetching data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            mGoogleSignInClient.revokeAccess().addOnCompleteListener(this, task1 -> {
                startActivity(new Intent(current_orders.this, orderlogin.class));
                finish();
            });
        });
    }
}