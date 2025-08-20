package com.inventory.core;

import com.inventory.dao.ProductDAO; // Import the correct DAO interface (now in the same 'dao' package)
import java.util.Objects;
import java.util.concurrent.TimeUnit;
// Removed ConcurrentMap/ConcurrentHashMap imports as SimpleProductDAO is gone.

/**
 * Manages inventory operations, acting as a service layer.
 * It delegates persistence operations to a ProductDAO.
 */
public class InventoryManager {

    private final ProductDAO productDAO;

    /**
     * Constructor to inject the ProductDAO.
     * @param productDAO The Data Access Object for Product entities.
     */
    public InventoryManager(ProductDAO productDAO) {
        this.productDAO = Objects.requireNonNull(productDAO, "ProductDAO cannot be null");
    }

    /* ----- Core Business Operations (delegating to DAO for persistence) ----- */

    /**
     * Adds a product to the inventory via the DAO.
     * @param product The product to add.
     * @throws InventoryException if product is null or already exists, or on database error.
     */
    public void addProduct(Product product) throws InventoryException {
        Objects.requireNonNull(product, "Product cannot be null");
        validateId(product.getId());
        productDAO.addProduct(product);
        System.out.println("InventoryManager: Product " + product.getId() + " added.");
    }

    /**
     * Retrieves a product from the inventory via the DAO.
     * @param productId The ID of the product to retrieve.
     * @return The Product object.
     * @throws InventoryException if product ID is invalid or product not found.
     */
    public Product getProduct(String productId) throws InventoryException {
        validateId(productId);
        return productDAO.getProductById(productId);
    }

    /**
     * Updates an existing product's details in the inventory via the DAO.
     * @param product The Product object with updated details.
     * @throws InventoryException if the product is not found or a database error occurs.
     */
    public void updateProduct(Product product) throws InventoryException {
        Objects.requireNonNull(product, "Product for update cannot be null");
        validateId(product.getId());
        productDAO.updateProduct(product);
        System.out.println("InventoryManager: Product " + product.getId() + " updated.");
    }

    /**
     * Removes a product from the inventory via the DAO.
     * @param productId The ID of the product to remove.
     * @return true if product was removed, false otherwise.
     * @throws InventoryException on database error.
     */
    public boolean removeProduct(String productId) throws InventoryException {
        validateId(productId);
        boolean removed = productDAO.deleteProduct(productId);
        if (removed) {
            System.out.println("InventoryManager: Product " + productId + " removed.");
        } else {
            System.out.println("InventoryManager: Product " + productId + " not found for removal.");
        }
        return removed;
    }

    /**
     * Calculates the current quantity of a product based on purchase and sales records.
     * This method would typically call a DAO method that aggregates data from `purchases` and `sales` tables.
     * @param productId The ID of the product for which to calculate the quantity.
     * @return The current available quantity of the product.
     * @throws InventoryException if the product is not found, or if a database error occurs.
     */
    public int calculateProductQuantity(String productId) throws InventoryException {
        validateId(productId);
        System.out.println("InventoryManager: Calculating quantity for product " + productId);
        return productDAO.calculateProductQuantity(productId);
    }

    /**
     * Transfers stock between products with deadlock prevention.
     * This operation fetches products, modifies their in-memory state (thread-safe),
     * and then persists the new quantities via the DAO.
     * @return true if transfer succeeded, false if timed out.
     * @throws InventoryException for business logic errors.
     * @throws InterruptedException if thread interrupted.
     */
    public boolean transferStock(String sourceId, String targetId, int amount,
                                 long timeout, TimeUnit unit)
            throws InventoryException, InterruptedException {

        // Validate parameters
        if (amount <= 0) {
            throw new InventoryException("Transfer amount must be positive");
        }
        if (sourceId.equals(targetId)) {
            throw new InventoryException("Cannot transfer to the same product");
        }

        // Get products from the DAO (these are fresh in-memory copies)
        // Ensure products exist before attempting lock
        Product source = getProduct(sourceId);
        Product target = getProduct(targetId);

        // Establish global lock ordering on in-memory Product objects
        Product first, second;
        if (source.getId().compareTo(target.getId()) < 0) {
            first = source;
            second = target;
        } else {
            first = target;
            second = source;
        }

        // Attempt lock acquisition on in-memory Product objects
        boolean firstLocked = first.getLock().tryLock(timeout, unit);
        if (!firstLocked) {
            System.out.println("DEBUG: Failed to acquire lock for first product: " + first.getId());
            return false;
        }

        try {
            boolean secondLocked = second.getLock().tryLock(timeout, unit);
            if (!secondLocked) {
                System.out.println("DEBUG: Failed to acquire lock for second product: " + second.getId());
                return false;
            }

            try {
                // Perform the in-memory transfer using Product's thread-safe methods
                if (source.getQuantity() < amount) {
                    throw new InventoryException("Insufficient stock in source product: " + sourceId +
                            ". Available: " + source.getQuantity() + ", Requested: " + amount);
                }
                source.removeStock(amount);
                target.addStock(amount);

                // Persist the changes back to the database via the DAO
                // IMPORTANT: For SQLite, transactions are typically managed at the Connection level.
                // You would ideally begin a transaction *before* the updates and commit/rollback.
                // For simplicity here, we're calling individual updates.
                productDAO.updateProduct(source); // Persist updated source
                productDAO.updateProduct(target); // Persist updated target

                System.out.println("InventoryManager: Stock transfer successful from " + source.getId() + " to " + target.getId());
                return true;
            } finally {
                second.getLock().unlock();
            }
        } finally {
            first.getLock().unlock();
        }
    }

    /* ----- Utility Methods ----- */

    private String validateId(String id) throws InventoryException {
        if (id == null || id.trim().isEmpty()) {
            throw new InventoryException("Product ID cannot be null or empty");
        }
        return id.trim();
    }

    /**
     * Shuts down any associated executor services.
     */
    public void shutdown() {
        System.out.println("InventoryManager shutdown initiated.");
    }
}