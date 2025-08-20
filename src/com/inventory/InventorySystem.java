package com.inventory;

import com.inventory.core.InventoryException;
import com.inventory.core.InventoryManager;
import com.inventory.core.Product;
import com.inventory.dao.ProductDAOImpl;

import java.util.concurrent.TimeUnit;

public class InventorySystem {

    public static void main(String[] args) {
        System.out.println("Starting Inventory System...");

        ProductDAOImpl productDAO = new ProductDAOImpl();
        InventoryManager inventoryManager = new InventoryManager(productDAO);

        try {
            // --- Product Management ---
            System.out.println("\n--- Product Management ---");

            Product laptop = new Product("Laptop Pro", "High-performance laptop", 1200.00,  "Electronics");
            inventoryManager.addProduct(laptop);

            Product keyboard = new Product("Mechanical Keyboard", "RGB gaming keyboard", 80.00,  "Peripherals");
            inventoryManager.addProduct(keyboard);

            // Check if products were added successfully and have IDs
            if (laptop.getId() == null || keyboard.getId() == null) {
                throw new InventoryException("Product IDs were not generated during addition");
            }

            // Use the actual generated IDs
            Product retrievedLaptop = inventoryManager.getProduct(laptop.getId());
            System.out.println("Retrieved Product: " + retrievedLaptop);

            // Update a product
            retrievedLaptop.setPrice(1150.00);
            retrievedLaptop.setDescription("Updated description: High-performance business laptop");
            inventoryManager.updateProduct(retrievedLaptop);
            Product updatedLaptop = inventoryManager.getProduct(laptop.getId());
            System.out.println("Updated Product: " + updatedLaptop);

            // Calculate product quantity
            int laptopQuantity = inventoryManager.calculateProductQuantity(laptop.getId());
            System.out.println("Current quantity for Laptop Pro (calculated from DB): " + laptopQuantity);

            // --- Stock Transfer Example ---
            System.out.println("\n--- Stock Transfer Example ---");
            Product productA = new Product("Product A", "General purpose item A", 10.0,  "General");
            Product productB = new Product("Product B", "General purpose item B", 20.0,  "General");

            inventoryManager.addProduct(productA);
            inventoryManager.addProduct(productB);

            // Check if products were added successfully
            if (productA.getId() == null || productB.getId() == null) {
                throw new InventoryException("Product IDs for transfer demo were not generated");
            }

            // Manually setting quantities for demo
            Product fetchedProductA = inventoryManager.getProduct(productA.getId());
            fetchedProductA.setQuantity(50);
            inventoryManager.updateProduct(fetchedProductA);
            System.out.println("Initial quantity of Product A (DB): " + inventoryManager.calculateProductQuantity(productA.getId()));

            Product fetchedProductB = inventoryManager.getProduct(productB.getId());
            fetchedProductB.setQuantity(30);
            inventoryManager.updateProduct(fetchedProductB);
            System.out.println("Initial quantity of Product B (DB): " + inventoryManager.calculateProductQuantity(productB.getId()));

            System.out.println("Attempting to transfer 10 units from Product A to Product B...");
            boolean transferSuccess = inventoryManager.transferStock(
                    productA.getId(),
                    productB.getId(),
                    10,
                    5,
                    TimeUnit.SECONDS
            );

            if (transferSuccess) {
                System.out.println("Stock transfer successful!");
                System.out.println("New quantity for Product A: " + inventoryManager.calculateProductQuantity(productA.getId()));
                System.out.println("New quantity for Product B: " + inventoryManager.calculateProductQuantity(productB.getId()));
            } else {
                System.out.println("Stock transfer failed or timed out.");
            }

            // --- Remove a product ---
            System.out.println("\n--- Product Removal ---");
            boolean removed = inventoryManager.removeProduct(keyboard.getId());
            System.out.println("Product " + keyboard.getId() + " removed: " + removed);
            try {
                inventoryManager.getProduct(keyboard.getId());
                System.out.println("ERROR: Product should not be found after removal!");
            } catch (InventoryException e) {
                System.out.println("Product " + keyboard.getId() + " correctly not found after removal: " + e.getMessage());
            }

        } catch (InventoryException e) {
            System.err.println("Inventory Operation Error: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Stock Transfer Interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            inventoryManager.shutdown();
            System.out.println("Inventory System shut down.");
        }
    }
}