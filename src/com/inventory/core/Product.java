package com.inventory.core;

import java.util.concurrent.locks.*;

public class Product {
    private final String id;
    private String name;
    private double price;
    private int quantity;
    final ReentrantLock lock = new ReentrantLock();

    public Product(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /* ----- Thread-Safe Methods ----- */

    public void addStock(int amount) throws IllegalArgumentException {
        lock.lock();
        try {
            if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
            quantity += amount;
        } finally {
            lock.unlock();
        }
    }

    public void removeStock(int amount) throws IllegalArgumentException {
        lock.lock();
        try {
            if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
            if (quantity < amount) throw new IllegalArgumentException("Insufficient stock");
            quantity -= amount;
        } finally {
            lock.unlock();
        }
    }

    /* ----- Getters/Setters ----- */
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getQuantity() { return quantity; }
}