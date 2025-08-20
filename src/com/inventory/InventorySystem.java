package com.inventory;

import com.inventory.core.InventoryException;
import com.inventory.core.InventoryManager;
import com.inventory.core.Product;
import com.inventory.dao.DBConnection; // Import DBConnection
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

            // Add a new product
            // For PostgreSQL SERIAL, you still typically provide a "logical" String ID
            // for your Product object. The DB will assign its own int ID.
            // When retrieving, you'll use the DB's int ID converted to String.
            Product laptop = new Product("LAP001", "Laptop Pro", "High-performance laptop", 1200.00, 0, "Electronics");
            inventoryManager.addProduct(laptop);

            Product keyboard = new Product("KB002", "Mechanical Keyboard", "RGB gaming keyboard", 80.00, 0, "Peripherals");
            inventoryManager.addProduct(keyboard);

            // To retrieve the product, you need its DB-generated integer ID.
            // In a real app, you might query by name, or the addProduct method might return the generated ID.
            // For now, let's assume we know some initial IDs inserted into DB (e.g., product_id 1 and 2)
            // Or you would have to query for product by name to get its actual DB ID.
            // For demo purposes, I'll use "1" and "2" as representative DB IDs.
            Product retrievedLaptop = inventoryManager.getProduct("1"); // Assuming LAP001 got DB ID 1
            System.out.println("Retrieved Product: " + retrievedLaptop);

            // Update a product
            retrievedLaptop.setPrice(1150.00);
            retrievedLaptop.setDescription("Updated description: High-performance business laptop");
            inventoryManager.updateProduct(retrievedLaptop);
            Product updatedLaptop = inventoryManager.getProduct("1");
            System.out.println("Updated Product: " + updatedLaptop);

            // Calculate product quantity
            int laptopQuantity = inventoryManager.calculateProductQuantity("1");
            System.out.println("Current quantity for Laptop Pro (calculated from DB): " + laptopQuantity);

            // --- Stock Transfer Example ---
            System.out.println("\n--- Stock Transfer Example ---");
            Product productA = new Product("PRODA", "Product A", 10.0, 0, "General");
            Product productB = new Product("PRODB", "Product B", 20.0, 0, "General");

            inventoryManager.addProduct(productA); // This will get DB ID 3 (assuming 1 and 2 already taken)
            inventoryManager.addProduct(productB); // This will get DB ID 4

            // Manually setting quantities for demo (in a real app, these would be from Purchases)
            // You need to retrieve the products by their actual DB-generated IDs.
            // For this demo, let's assume they got DB IDs "3" and "4"
            Product fetchedProductA = inventoryManager.getProduct("3");
            fetchedProductA.setQuantity(50);
            inventoryManager.updateProduct(fetchedProductA); // Persist quantity update
            System.out.println("Initial quantity of Product A (DB): " + inventoryManager.calculateProductQuantity("3"));

            Product fetchedProductB = inventoryManager.getProduct("4");
            fetchedProductB.setQuantity(30);
            inventoryManager.updateProduct(fetchedProductB); // Persist quantity update
            System.out.println("Initial quantity of Product B (DB): " + inventoryManager.calculateProductQuantity("4"));

            System.out.println("Attempting to transfer 10 units from Product A to Product B...");
            boolean transferSuccess = inventoryManager.transferStock("3", "4", 10, 5, TimeUnit.SECONDS);

            if (transferSuccess) {
                System.out.println("Stock transfer successful!");
                System.out.println("New quantity for Product A: " + inventoryManager.calculateProductQuantity("3"));
                System.out.println("New quantity for Product B: " + inventoryManager.calculateProductQuantity("4"));
            } else {
                System.out.println("Stock transfer failed or timed out.");
            }

            // --- Remove a product ---
            System.out.println("\n--- Product Removal ---");
            // Assuming KB002 got DB ID 2
            boolean removed = inventoryManager.removeProduct("2");
            System.out.println("Product 2 removed: " + removed);
            try {
                inventoryManager.getProduct("2");
            } catch (InventoryException e) {
                System.out.println("Product 2 correctly not found after removal: " + e.getMessage());
            }

        } catch (InventoryException e) {
            System.err.println("Inventory Operation Error: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Stock Transfer Interrupted: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore interrupt status
        } finally {
            // CRUCIAL: Shut down HikariCP connection pool
            inventoryManager.shutdown(); // (Placeholder, doesn't do much in current InventoryManager)
            DBConnection.shutdown(); // This will close the HikariCP pool
            System.out.println("Inventory System shut down.");
        }
    }
}