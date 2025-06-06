package com.example.grabandgo;

public class Order {
    private String orderId;
    private double totalAmount;
    private double commission;
    private String address;

    // Default constructor required for calls to DataSnapshot.getValue(Order.class)
    public Order() {
    }

    public Order(String orderId, double totalAmount, double commission, String address) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.commission = commission;
        this.address = address;
    }

    public String getOrderId() {
        return orderId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getCommission() {
        return commission;
    }

    public String getAddress() {
        return address;
    }
}

