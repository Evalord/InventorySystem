package com.inventory.ui;

import com.inventory.core.InventoryException;
import com.inventory.core.InventoryManager;
import com.inventory.core.Product;
import com.inventory.dao.ProductDAOImpl;

import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class MainMenu {

    private InventoryManager inventoryManager;
    private Scanner scanner;

    public MainMenu() {
        this.inventoryManager = new InventoryManager(new ProductDAOImpl());
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("Welcome to the Inventory and Storage Management System!");
        int choice;
        do {
            displayMenu();
            choice = getUserChoice();

            try {
                switch (choice) {
                    case 1:
                        addProduct();
                        break;
                    case 2:
                        updateProduct();
                        break;
                    case 3:
                        deleteProduct();
                        break;
                    case 4:
                        viewProduct();
                        break;
                    case 5:
                        calculateProductQuantity();
                        break;
                    case 6:
                        transferStock();
                        break;
                    case 7:
                        generateReport();
                        break;
                    case 8:
                        System.out.println("Exiting system. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 8.");
                }
            } catch (InventoryException e) {
                System.err.println("Operation failed: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("An unexpected error occurred: " + e.getMessage());
                e.printStackTrace();
            }

            if (choice != 8) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }

        } while (choice != 8);

        scanner.close();
        inventoryManager.shutdown();
    }

    private void displayMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Add New Product");
        System.out.println("2. Update Product Details");
        System.out.println("3. Delete Product");
        System.out.println("4. View Product");
        System.out.println("5. Calculate Product Quantity");
        System.out.println("6. Transfer Stock Between Products");
        System.out.println("7. Generate Inventory Report");
        System.out.println("8. Exit");
        System.out.print("Enter your choice: ");
    }

    private int getUserChoice() {
        try {
            return scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.nextLine();
            return -1;
        } finally {
            scanner.nextLine();
        }
    }

    private void addProduct() throws InventoryException {
        System.out.println("\n--- Add New Product ---");
        System.out.print("Enter Product Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Product Description: ");
        String description = scanner.nextLine().trim();

        System.out.print("Enter Product Price: ");
        double price = getDoubleInput();

        System.out.print("Enter Initial Quantity: ");
        int quantity = getIntInput();

        System.out.print("Enter Product Category: ");
        String category = scanner.nextLine().trim();

        Product newProduct = new Product(null, name, description, price, quantity, category);
        inventoryManager.addProduct(newProduct);
        System.out.println("Product '" + name + "' added successfully with ID: " + newProduct.getId() + "!");
    }

    private void updateProduct() throws InventoryException {
        System.out.println("\n--- Update Product ---");
        System.out.println("Search by: 1. Name  2. ID");
        System.out.print("Enter choice: ");
        int searchChoice = getIntInput();

        Product existingProduct = null;

        if (searchChoice == 1) {
            System.out.print("Enter product name: ");
            String name = scanner.nextLine().trim();
            existingProduct = inventoryManager.getProductByName(name);
        } else if (searchChoice == 2) {
            System.out.print("Enter product ID: ");
            String id = scanner.nextLine().trim();
            existingProduct = inventoryManager.getProduct(id);
        } else {
            System.out.println("Invalid choice.");
            return;
        }

        if (existingProduct == null) {
            System.out.println("Product not found.");
            return;
        }

        System.out.println("Current Details:");
        System.out.println(existingProduct);

        System.out.print("Enter New Name (current: " + existingProduct.getName() + "): ");
        String newName = scanner.nextLine().trim();
        if (!newName.isEmpty()) {
            existingProduct.setName(newName);
        }

        System.out.print("Enter New Description (current: " + existingProduct.getDescription() + "): ");
        String newDesc = scanner.nextLine().trim();
        if (!newDesc.isEmpty()) {
            existingProduct.setDescription(newDesc);
        }

        System.out.print("Enter New Price (current: " + existingProduct.getPrice() + "): ");
        double newPrice = getDoubleInput();
        if (newPrice >= 0) {
            existingProduct.setPrice(newPrice);
        }

        System.out.print("Enter New Quantity (current: " + existingProduct.getQuantity() + "): ");
        int newQuantity = getIntInput();
        if (newQuantity >= 0) {
            existingProduct.setQuantity(newQuantity);
        }

        System.out.print("Enter New Category (current: " + existingProduct.getCategory() + "): ");
        String newCategory = scanner.nextLine().trim();
        if (!newCategory.isEmpty()) {
            existingProduct.setCategory(newCategory);
        }

        inventoryManager.updateProduct(existingProduct);
        System.out.println("Product updated successfully!");
    }

    private void deleteProduct() throws InventoryException {
        System.out.println("\n--- Delete Product ---");
        System.out.print("Enter product ID to delete: ");
        String id = scanner.nextLine().trim();

        boolean deleted = inventoryManager.removeProduct(id);
        if (deleted) {
            System.out.println("Product deleted successfully!");
        } else {
            System.out.println("Product not found or could not be deleted.");
        }
    }

    private void viewProduct() throws InventoryException {
        System.out.println("\n--- View Product ---");
        System.out.println("Search by: 1. Name  2. ID");
        System.out.print("Enter choice: ");
        int searchChoice = getIntInput();

        Product product = null;

        if (searchChoice == 1) {
            System.out.print("Enter product name: ");
            String name = scanner.nextLine().trim();
            product = inventoryManager.getProductByName(name);
        } else if (searchChoice == 2) {
            System.out.print("Enter product ID: ");
            String id = scanner.nextLine().trim();
            product = inventoryManager.getProduct(id);
        } else {
            System.out.println("Invalid choice.");
            return;
        }

        if (product != null) {
            System.out.println("\nProduct Details:");
            System.out.println(product);
        } else {
            System.out.println("Product not found.");
        }
    }

    private void calculateProductQuantity() throws InventoryException {
        System.out.println("\n--- Calculate Product Quantity ---");
        System.out.print("Enter product ID: ");
        String id = scanner.nextLine().trim();

        int quantity = inventoryManager.calculateProductQuantity(id);
        System.out.println("Current calculated quantity: " + quantity);
    }

    private void transferStock() throws InventoryException {
        System.out.println("\n--- Transfer Stock ---");
        System.out.print("Enter Source Product ID: ");
        String sourceId = scanner.nextLine().trim();
        System.out.print("Enter Destination Product ID: ");
        String destId = scanner.nextLine().trim();
        System.out.print("Enter quantity to transfer: ");
        int quantity = getIntInput();

        try {
            boolean success = inventoryManager.transferStock(sourceId, destId, quantity, 10, TimeUnit.SECONDS);
            if (success) {
                System.out.println("Stock transfer completed successfully!");
            } else {
                System.out.println("Stock transfer failed (timeout or insufficient stock).");
            }
        } catch (InterruptedException e) {
            System.err.println("Transfer interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void generateReport() {
        System.out.println("\n--- Generate Report ---");
        System.out.println("Report generation feature coming soon!");
        // Placeholder for report generation logic
    }

    private int getIntInput() {
        while (true) {
            try {
                int input = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                if (input < 0) {
                    System.out.print("Please enter a non-negative number: ");
                    continue;
                }
                return input;
            } catch (InputMismatchException e) {
                System.out.print("Invalid input. Please enter a whole number: ");
                scanner.nextLine(); // Clear invalid input
            }
        }
    }

    private double getDoubleInput() {
        while (true) {
            try {
                double input = scanner.nextDouble();
                scanner.nextLine(); // Consume newline
                if (input < 0) {
                    System.out.print("Please enter a non-negative number: ");
                    continue;
                }
                return input;
            } catch (InputMismatchException e) {
                System.out.print("Invalid input. Please enter a valid number: ");
                scanner.nextLine(); // Clear invalid input
            }
        }
    }

    public static void main(String[] args) {
        MainMenu menu = new MainMenu();
        menu.start();
    }
}