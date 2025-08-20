package com.inventory.core;

import java.util.Objects;
import java.util.concurrent.locks.*;

/**
 * Thread-safe product entity with inventory management capabilities
 */
public class Product {
    private String id;
    private String name;
    private double price;
    private int quantity;
    private String category;
    private final Lock lock = new ReentrantLock();


    public Product(String id, String name) {
        this(id, name, 0.0, 0, null);
    }

    public Product(String id, String name, double price, int quantity, String category) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
    }

    /* ----- Thread-Safe Methods ----- */

    /**
     * Adds stock to product inventory
     * @param amount Positive quantity to add
     * @throws IllegalArgumentException if amount is not positive
     */
    public void addStock(int amount) throws IllegalArgumentException {
        lock.lock();
        try {
            if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
            quantity += amount;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes stock from product inventory
     * @param amount Positive quantity to remove
     * @throws IllegalArgumentException if amount invalid or insufficient stock
     */
    public void removeStock(int amount) throws IllegalArgumentException {
        lock.lock();
        try {
            if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
            if (quantity < amount) {
                throw new IllegalArgumentException(
                        String.format("Insufficient stock. Available: %d, Requested: %d",
                                quantity, amount));
            }
            quantity -= amount;
        } finally {
            lock.unlock();
        }
    }

    /* ----- Getters/Setters ----- */
    public String getId() { return id; }

    public Lock getLock(){
        return this.lock;
    }

    public String getName() {
        lock.lock();
        try {
            return name;
        } finally {
            lock.unlock();
        }
    }

    public void setName(String name) {
        lock.lock();
        try {
            this.name = name;
        } finally {
            lock.unlock();
        }
    }

    public double getPrice() {
        lock.lock();
        try {
            return price;
        } finally {
            lock.unlock();
        }
    }

    public void setPrice(double price) {
        lock.lock();
        try {
            this.price = price;
        } finally {
            lock.unlock();
        }
    }

    public int getQuantity() {
        lock.lock();
        try {
            return quantity;
        } finally {
            lock.unlock();
        }
    }

    public String getCategory() {
        lock.lock();
        try {
            return category;
        } finally {
            lock.unlock();
        }
    }

    public void setCategory(String category) {
        lock.lock();
        try {
            this.category = category;
        } finally {
            lock.unlock();
        }
    }

    /* ----- Object Overrides ----- */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        lock.lock();
        try {
            return "Product{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", price=" + price +
                    ", quantity=" + quantity +
                    ", category='" + category + '\'' +
                    '}';
        } finally {
            lock.unlock();
        }
    }
}
