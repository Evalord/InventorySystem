package com.inventory.dao;

import com.inventory.core.Product;
import com.inventory.core.InventoryException;

/**
 * Data Access Object (DAO) interface for Product entities.
 * Defines the contract for interacting with product data in the database.
 */
public interface ProductDAO {

    /**
     * Inserts a new product record into the database.
     * @param product The Product object to add. Its ID should be unique.
     * @throws InventoryException if a product with the same ID already exists,
     * or if a database error occurs.
     */
    void addProduct(Product product) throws InventoryException;

    /**
     * Retrieves a product record from the database by its ID.
     * @param productId The ID of the product to retrieve.
     * @return The Product object corresponding to the given ID.
     * @throws InventoryException if the product is not found,
     * or if a database error occurs.
     */
    Product getProductById(String productId) throws InventoryException;

    /**
     * Updates an existing product record in the database.
     * This method assumes the product's ID already exists in the database.
     * @param product The Product object with updated details (ID used for lookup).
     * @throws InventoryException if the product to update is not found,
     * or if a database error occurs.
     */
    void updateProduct(Product product) throws InventoryException;

    /**
     * Deletes a product record from the database by its ID.
     * @param productId The ID of the product to delete.
     * @return true if the product was successfully deleted, false if not found.
     * @throws InventoryException if a database error occurs.
     */
    boolean deleteProduct(String productId) throws InventoryException;

    /**
     * Calculates the current quantity of a product based on purchase and sales records.
     * This is a critical method for accurately reflecting inventory levels from transactions.
     * @param productId The ID of the product for which to calculate the quantity.
     * @return The current available quantity of the product.
     * @throws InventoryException if the product is not found,
     * or if a database error occurs.
     */
    int calculateProductQuantity(String productId) throws InventoryException;
}