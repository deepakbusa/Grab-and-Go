package com.example.grabandgo;

import java.util.Objects;

public class Items {
    private String name;
    private int quantity;
    private double cost;

    public Items(String name, int quantity, double cost) {
        this.name = name;
        this.quantity = quantity;
        this.cost = cost;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getCost() {
        return cost;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Items)) return false;

        Items item = (Items) o;
        return quantity == item.quantity &&
                Double.compare(item.cost, cost) == 0 &&
                name.equals(item.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, quantity, cost);
    }

}
