package com.inventory.core;

import java.util.Objects;
import java.util.concurrent.locks.*;

/**
 * Thread-safe product entity with inventory management capabilities.
 */
public class Product {
    private String id; // This MUST be int to match PostgreSQL's SERIAL
    private String name;
    private String description;
    private double price;
    private int quantity;
    private String category;
    private final Lock lock = new ReentrantLock();

    // Constructor for NEW products (ID is not known yet, DB will generate it)
    public Product(String name, String description, double price, String category) {
        this(null,name, description, price, 0, category);
    }

    // Constructor for retrieving EXISTING products from the database
    // or for comprehensive initialization where ID is known.
    public Product(String id, String name, String description, double price, int quantity, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
    }

    /* ----- Getters/Setters ----- */
    // Getter for ID (returns int)
    public String getId() {
        return id;
    }

    // Setter for ID (used by DAO after DB insertion)
    public void setId(String id) {
        this.id = id;
    }

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

    public String getDescription() {
        lock.lock();
        try {
            return description;
        } finally {
            lock.unlock();
        }
    }

    public void setDescription(String description) {
        lock.lock();
        try {
            this.description = description;
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

    public void setQuantity(int quantity) {
        lock.lock();
        try {
            this.quantity = quantity;
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

    /* ----- Thread-Safe Methods (remain as they were) ----- */

    /**
     * Adds stock to product inventory.
     * @param amount Positive quantity to add.
     * @throws IllegalArgumentException if amount is not positive.
     */
    public void addStock(int amount) throws IllegalArgumentException {
        lock.lock();
        try {
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }
            quantity += amount;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes stock from product inventory.
     * @param amount Positive quantity to remove.
     * @throws IllegalArgumentException if amount invalid or insufficient stock.
     */
    public void removeStock(int amount) throws IllegalArgumentException {
        lock.lock();
        try {
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }
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

    /* ----- Object Overrides ----- */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        // Compare based on int ID
        return id == product.id; // Corrected comparison for int
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Corrected hashing for int
    }

    @Override
    public String toString() {
        lock.lock();
        try {
            return "Product{" +
                    "id=" + id + // Removed single quotes for int
                    ", name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", price=" + price +
                    ", quantity=" + quantity +
                    ", category='" + category + '\'' +
                    '}';
        } finally {
            lock.unlock();
        }
    }
}