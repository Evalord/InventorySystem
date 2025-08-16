package com.inventory.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Collection;
import java.util.Collections;

/**
 * Manages inventory operations with thread-safe implementations.
 * Uses ConcurrentHashMap for thread-safe product storage.
 */
public class InventoryManager {
    private final ConcurrentMap<String, Product> inventory;

    public InventoryManager() {
        this.inventory = new ConcurrentHashMap<>();
    }

    // Validation helper (now properly placed before usage)
    private String validateProductId(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        return productId;
    }

    /**
     * Adds a new product to inventory
     * @param product Product to add
     * @throws IllegalArgumentException if product is null or already exists
     */
    public synchronized void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        String productId = validateProductId(product.getId());
        if (inventory.containsKey(productId)) {
            throw new IllegalArgumentException("Product with ID " + productId + " already exists");
        }
        inventory.put(productId, product);
    }

    /**
     * Removes a product from inventory
     * @param productId ID of product to remove
     * @return removed Product or null if not found
     */
    public synchronized Product removeProduct(String productId) {
        return inventory.remove(validateProductId(productId));
    }

    /**
     * Gets a product by ID
     * @param productId ID to search for
     * @return Product or null if not found
     */
    public Product getProduct(String productId) {
        return inventory.get(validateProductId(productId));
    }

    /**
     * Updates product stock (thread-safe)
     * @param productId ID of product to update
     * @param amount Positive to add, negative to remove
     * @throws IllegalArgumentException if invalid amount or insufficient stock
     */
    public void updateStock(String productId, int amount) {
        String validatedId = validateProductId(productId);
        Product product = inventory.get(validatedId);
        if (product == null) {
            throw new IllegalArgumentException("Product with ID " + validatedId + " not found");
        }

        synchronized (product) {  // Fine-grained locking
            if (amount > 0) {
                product.addStock(amount);
            } else if (amount < 0) {
                product.removeStock(-amount);
            } else {
                throw new IllegalArgumentException("Amount cannot be zero");
            }
        }
    }

    /**
     * Returns all products (unmodifiable for thread safety)
     * @return Collection of all products
     */
    public Collection<Product> getAllProducts() {
        return Collections.unmodifiableCollection(inventory.values());
    }

    /**
     * Gets total inventory value
     * @return Sum of (price * quantity) for all products
     */
    public double getTotalInventoryValue() {
        return inventory.values().parallelStream()
                .mapToDouble(p -> p.getPrice() * p.getQuantity())
                .sum();
    }
}
