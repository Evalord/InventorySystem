package com.inventory.core;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class InventoryManager {
    private final ConcurrentMap<String, Product> inventory = new ConcurrentHashMap<>();
    private final ReadWriteLock inventoryLock = new ReentrantReadWriteLock();
    private final ExecutorService reportExecutor = Executors.newWorkStealingPool();

    // Custom exception for inventory operations
    public static class InventoryException extends Exception {
        public InventoryException(String message) { super(message); }
        public InventoryException(String message, Throwable cause) { super(message, cause); }
    }

    /* ----- Core Thread-Safe Operations ----- */

    /**
     * Transfers stock between products with deadlock prevention
     * @return true if transfer succeeded, false if timed out
     * @throws InventoryException for business logic errors
     * @throws InterruptedException if thread interrupted
     */
    public boolean transferStock(String sourceId, String targetId, int amount,
                                 long timeout, TimeUnit unit)
            throws InventoryException, InterruptedException {

        // Validate parameters
        if (amount <= 0) throw new InventoryException("Transfer amount must be positive");
        if (sourceId.equals(targetId)) throw new InventoryException("Cannot transfer to same product");

        // Get products with thread-safe check
        Product source = getProduct(sourceId);
        Product target = getProduct(targetId);
        if (source == null || target == null) {
            throw new InventoryException("Product not found: " + (source == null ? sourceId : targetId));
        }

        // Establish global lock ordering
        Product first, second;
        if (source.getId().compareTo(target.getId()) < 0) {
            first = source;
            second = target;
        } else {
            first = target;
            second = source;
        }

        // Attempt lock acquisition
        boolean firstLocked = first.lock.tryLock(timeout, unit);
        if (!firstLocked) return false;

        try {
            boolean secondLocked = second.lock.tryLock(timeout, unit);
            if (!secondLocked) return false;

            try {
                // Perform the transfer
                source.removeStock(amount);
                target.addStock(amount);
                return true;
            } finally {
                second.lock.unlock();
            }
        } finally {
            first.lock.unlock();
        }
    }

    /* ----- Supporting Methods ----- */

    public Product getProduct(String productId) throws InventoryException {
        try {
            inventoryLock.readLock().lock();
            return inventory.get(validateId(productId));
        } finally {
            inventoryLock.readLock().unlock();
        }
    }

    public void addProduct(Product product) throws InventoryException {
        Objects.requireNonNull(product, "Product cannot be null");
        inventoryLock.writeLock().lock();
        try {
            String id = validateId(product.getId());
            if (inventory.putIfAbsent(id, product) != null) {
                throw new InventoryException("Product already exists: " + id);
            }
        } finally {
            inventoryLock.writeLock().unlock();
        }
    }

    /* ----- Utility Methods ----- */

    private String validateId(String id) throws InventoryException {
        if (id == null || id.trim().isEmpty()) {
            throw new InventoryException("Product ID cannot be null or empty");
        }
        return id;
    }

    public void shutdown() {
        reportExecutor.shutdown();
        try {
            if (!reportExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                reportExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            reportExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}