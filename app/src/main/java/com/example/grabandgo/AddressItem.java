package com.example.grabandgo;

public class AddressItem {
    private String id; // Unique identifier for the address
    private String mobileNumber;
    private String roomNumber;
    private String block;
    private String address;

    // Default constructor required for calls to DataSnapshot.getValue(AddressItem.class)
    public AddressItem() {}

    public AddressItem(String id, String mobileNumber, String roomNumber, String block, String address) {
        this.id = id;
        this.mobileNumber = mobileNumber;
        this.roomNumber = roomNumber;
        this.block = block;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getBlock() {
        return block;
    }

    public String getAddress() {
        return address;
    }
}
