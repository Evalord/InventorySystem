package com.inventory.dao;

import com.inventory.core.Product;
import com.inventory.core.InventoryException;

import java.sql.*;

public class ProductDAOImpl implements ProductDAO {

    // No need for enableForeignKeys() for PostgreSQL as it's enabled by default
    // and handled by the database itself.

    @Override
    public void addProduct(Product product) throws InventoryException {
        // PostgreSQL's SERIAL primary key is auto-generated.
        // DO NOT include 'product_id' in the INSERT statement.
        String sql = "INSERT INTO products (name, description, price, category) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setString(1, product.getName());
                pstmt.setString(2, product.getDescription());
                pstmt.setDouble(3, product.getPrice());
                pstmt.setString(4, product.getCategory());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new InventoryException("Creating product failed, no rows affected.");
                }

                // Retrieve the database-generated ID (product_id)
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1); // PostgreSQL returns the SERIAL column
                        // You can optionally update the Product object's ID if needed
                        // product.setId(String.valueOf(generatedId)); // If Product class had setId()
                        System.out.println("ProductDAOImpl: Product added with generated ID: " + generatedId);
                    } else {
                        throw new InventoryException("Creating product failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException e) {
            // PostgreSQL unique constraint violation error code is '23505'
            if (e.getSQLState().equals("23505")) {
                throw new InventoryException("A product with the same name (or other unique field) already exists.", e);
            }
            throw new InventoryException("Database error while adding product: " + e.getMessage(), e);
        }
    }

    @Override
    public Product getProductById(String productId) throws InventoryException {
        // Convert the String productId from the Product object to an int for database lookup.
        int id;
        try {
            id = Integer.parseInt(productId);
        } catch (NumberFormatException e) {
            throw new InventoryException("Invalid Product ID format: " + productId + ". ID must be a valid integer.", e);
        }

        String sql = "SELECT product_id, name, description, price, category FROM products WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id); // Use setInt for the integer ID

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Convert the int product_id from the database back to a String for the Product object.
                    return new Product(
                            String.valueOf(rs.getInt("product_id")),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            0, // Quantity will be calculated separately by calculateProductQuantity
                            rs.getString("category")
                    );
                } else {
                    throw new InventoryException("Product not found with ID: " + productId);
                }
            }
        } catch (SQLException e) {
            throw new InventoryException("Database error while retrieving product: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateProduct(Product product) throws InventoryException {
        // Convert the String productId from the Product object to an int for database lookup.
        int id;
        try {
            id = Integer.parseInt(product.getId());
        } catch (NumberFormatException e) {
            throw new InventoryException("Invalid Product ID format for update: " + product.getId() + ". ID must be a valid integer.", e);
        }

        String sql = "UPDATE products SET name = ?, description = ?, price = ?, category = ? WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, product.getName());
                pstmt.setString(2, product.getDescription());
                pstmt.setDouble(3, product.getPrice());
                pstmt.setString(4, product.getCategory());
                pstmt.setInt(5, id);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new InventoryException("Product not found for update with ID: " + product.getId());
                }
            }
        } catch (SQLException e) {
            throw new InventoryException("Database error while updating product: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteProduct(String productId) throws InventoryException {
        // Convert the String productId to an int for database deletion.
        int id;
        try {
            id = Integer.parseInt(productId);
        } catch (NumberFormatException e) {
            throw new InventoryException("Invalid Product ID format for deletion: " + productId + ". ID must be a valid integer.", e);
        }

        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            throw new InventoryException("Database error while deleting product: " + e.getMessage(), e);
        }
    }

    @Override
    public int calculateProductQuantity(String productId) throws InventoryException {
        // Convert the String productId to an int for database calculations.
        int id;
        try {
            id = Integer.parseInt(productId);
        } catch (NumberFormatException e) {
            throw new InventoryException("Invalid Product ID format for quantity calculation: " + productId + ". ID must be a valid integer.", e);
        }

        int totalPurchased = 0;
        int totalSold = 0;

        String purchaseSql = "SELECT COALESCE(SUM(quantity), 0) FROM purchases WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(purchaseSql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        totalPurchased = rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new InventoryException("Database error while calculating purchases for product " + productId + ": " + e.getMessage(), e);
        }

        String saleSql = "SELECT COALESCE(SUM(quantity), 0) FROM sales WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(saleSql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        totalSold = rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new InventoryException("Database error while calculating sales for product " + productId + ": " + e.getMessage(), e);
        }

        return totalPurchased - totalSold;
    }
}