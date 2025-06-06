package com.example.grabandgo;

public class CartItem {
    public String name;
    public double price;
    public int quantity;
    public String shopName; // Add shop name to the CartItem

    public CartItem(String name, double price, int quantity, String shopName) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.shopName = shopName;
    }

    // Optionally, a default constructor can be added for Firebase retrieval
    public CartItem() {
        // Default constructor required for calls to DataSnapshot.getValue(CartItem.class)
    }
}
