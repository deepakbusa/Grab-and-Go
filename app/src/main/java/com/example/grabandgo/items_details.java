package com.example.grabandgo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class items_details extends AppCompatActivity {
    private LinearLayout itemsContainer;
    private TextView noItemsMessage;
    private TextView shopNameTextView;
    private List<Item> itemList;
    private Set<Item> selectedItems;
    private String shopName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_details);

        itemsContainer = findViewById(R.id.items_container);
        noItemsMessage = findViewById(R.id.no_items_message);
        shopNameTextView = findViewById(R.id.shop_name_text_view); // Ensure this TextView exists in XML
        Button finishButton = findViewById(R.id.finish_button);

        selectedItems = new HashSet<>();

        // Get shop name from Intent
        shopName = getIntent().getStringExtra("shopName");
        if (shopName != null) {
            shopNameTextView.setText("Shop: " + shopName);
        } else {
            shopNameTextView.setText("Shop: Unknown");
        }

        // Load items from Firebase
        loadItemsFromFirebase();

        finishButton.setOnClickListener(v -> {
            removeSelectedItems();
            Toast.makeText(this, "Selected items removed!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(items_details.this, sucess_item.class);
            startActivity(intent);
        });
    }

    private void loadItemsFromFirebase() {
        if (shopName == null || shopName.isEmpty()) {
            Toast.makeText(items_details.this, "Shop name not available", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("shops").child(shopName).child("items");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Item item = data.getValue(Item.class);
                    if (item != null) {
                        itemList.add(item);
                    }
                }
                displayItems();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(items_details.this, "Failed to load items", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void displayItems() {
        itemsContainer.removeAllViews();

        if (itemList.isEmpty()) {
            noItemsMessage.setVisibility(View.VISIBLE);
        } else {
            noItemsMessage.setVisibility(View.GONE);
            for (Item item : itemList) {
                addItemView(item);
            }
        }
    }

    private void addItemView(Item item) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        itemLayout.setPadding(20, 20, 20, 20);
        itemLayout.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
        itemLayout.setElevation(8);
        itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView itemTextView = new TextView(this);
        itemTextView.setText(item.name + " - ₹" + item.price);
        itemTextView.setTextSize(18);
        itemTextView.setPadding(10, 10, 10, 10);

        Button selectButton = new Button(this);
        selectButton.setText("Remove");

        selectButton.setOnClickListener(v -> {
            if (selectedItems.contains(item)) {
                selectedItems.remove(item);
                selectButton.setText("Remove");
                itemLayout.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
            } else {
                selectedItems.add(item);
                selectButton.setText("Selected");
                itemLayout.setBackgroundColor(0xFFE0E0E0);
            }
        });

        itemLayout.addView(itemTextView);
        itemLayout.addView(selectButton);
        itemsContainer.addView(itemLayout);
    }

    private void removeSelectedItems() {
        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "No items selected to remove", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference shopRef = FirebaseDatabase.getInstance().getReference("shops").child(shopName).child("items");

        for (Item item : selectedItems) {
            shopRef.orderByChild("name").equalTo(item.name).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        itemSnapshot.getRef().removeValue().addOnSuccessListener(aVoid -> {
                            Log.d("Firebase", "Item removed: " + item.name);
                        }).addOnFailureListener(e -> {
                            Log.e("Firebase", "Failed to remove item: " + item.name, e);
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Database error: " + error.getMessage());
                }
            });
        }

        selectedItems.clear();
        Toast.makeText(this, "Selected items removed from Firebase!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(items_details.this, sucess_item.class);
        startActivity(intent);
    }


    // Item class
    public static class Item {
        public String name;
        public double price;

        public Item() {
        }

        public Item(String name, double price) {
            this.name = name;
            this.price = price;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Item)) return false;
            Item item = (Item) o;
            return Double.compare(item.price, price) == 0 && Objects.equals(name, item.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, price);
        }
    }
}
