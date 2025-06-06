package com.example.grabandgo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class AddAddressActivity extends AppCompatActivity {
    private EditText etMobileNumber, etRoomNumber, etBlock, etAddress;
    private Button btnSaveAddress;
    private androidx.appcompat.widget.AppCompatImageButton btnBack; // Correct type for image button
    private DatabaseReference addressRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        // Initialize Firebase Database Reference for user's addresses
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Replace with the actual user ID (from authentication)
        addressRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("addresses");

        // UI Components
        etMobileNumber = findViewById(R.id.et_mobile_number);
        etRoomNumber = findViewById(R.id.et_room_number);
        etBlock = findViewById(R.id.et_block);
        etAddress = findViewById(R.id.et_address);
        btnSaveAddress = findViewById(R.id.btn_save_address);
        btnBack = findViewById(R.id.btn_back); // Initialize the back button as an AppCompatImageButton

        // Save address button click listener
        btnSaveAddress.setOnClickListener(v -> saveAddress());

        // Back button click listener
        btnBack.setOnClickListener(v -> finish()); // Close the activity and return to the previous page
    }

    private void saveAddress() {
        String mobileNumber = etMobileNumber.getText().toString().trim();
        String roomNumber = etRoomNumber.getText().toString().trim();
        String block = etBlock.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (mobileNumber.isEmpty() || roomNumber.isEmpty() || block.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = UUID.randomUUID().toString(); // Generate a unique ID for each address
        AddressItem addressItem = new AddressItem(id, mobileNumber, roomNumber, block, address);
        addressRef.child(id).setValue(addressItem).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Address saved successfully!", Toast.LENGTH_SHORT).show();
                finish(); // Close activity
            } else {
                Toast.makeText(this, "Failed to save address.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
