package com.inventory.dao;

import com.inventory.core.Product;
import com.inventory.core.InventoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAOImpl implements ProductDAO {

    @Override
    public void addProduct(Product product) throws InventoryException {
        String sql = "INSERT INTO products (name, description, price, quantity, category) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getDescription());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setInt(4, product.getQuantity());
            pstmt.setString(5, product.getCategory());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new InventoryException("Creating product failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    product.setId(String.valueOf(generatedId));
                    System.out.println("ProductDAOImpl: Product added with generated ID: " + generatedId);
                } else {
                    throw new InventoryException("Creating product failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new InventoryException("Database error while adding product: " + e.getMessage(), e);
        }
    }

    @Override
    public Product getProductById(String productId) throws InventoryException {
        String sql = "SELECT product_id, name, description, price, quantity, category FROM products WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int id = Integer.parseInt(productId);
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                            String.valueOf(rs.getInt("product_id")),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getInt("quantity"),
                            rs.getString("category")
                    );
                } else {
                    throw new InventoryException("Product not found with ID: " + productId);
                }
            }
        } catch (NumberFormatException e) {
            throw new InventoryException("Invalid Product ID format: " + productId, e);
        } catch (SQLException e) {
            throw new InventoryException("Database error while retrieving product: " + e.getMessage(), e);
        }
    }

    @Override
    public Product getProductByName(String productName) throws InventoryException {
        String sql = "SELECT product_id, name, description, price, quantity, category FROM products WHERE name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productName.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                            String.valueOf(rs.getInt("product_id")),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getInt("quantity"),
                            rs.getString("category")
                    );
                } else {
                    throw new InventoryException("Product not found with name: " + productName);
                }
            }
        } catch (SQLException e) {
            throw new InventoryException("Database error while retrieving product by name: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateProduct(Product product) throws InventoryException {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, quantity = ?, category = ? WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getDescription());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setInt(4, product.getQuantity());
            pstmt.setString(5, product.getCategory());

            int id = Integer.parseInt(product.getId());
            pstmt.setInt(6, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new InventoryException("Product not found for update with ID: " + product.getId());
            }
        } catch (NumberFormatException e) {
            throw new InventoryException("Invalid Product ID format: " + product.getId(), e);
        } catch (SQLException e) {
            throw new InventoryException("Database error while updating product: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteProduct(String productId) throws InventoryException {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int id = Integer.parseInt(productId);
            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (NumberFormatException e) {
            throw new InventoryException("Invalid Product ID format: " + productId, e);
        } catch (SQLException e) {
            throw new InventoryException("Database error while deleting product: " + e.getMessage(), e);
        }
    }

    @Override
    public int calculateProductQuantity(String productId) throws InventoryException {
        // Implementation for calculating quantity from purchases/sales
        // This is a placeholder - you'll need to implement based on your database schema
        try {
            Product product = getProductById(productId);
            return product.getQuantity(); // For now, just return stored quantity
        } catch (InventoryException e) {
            throw new InventoryException("Error calculating quantity for product: " + productId, e);
        }
    }

    @Override
    public List<Product> getAllProducts() throws InventoryException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, name, description, price, quantity, category FROM products ORDER BY name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Product product = new Product(
                        String.valueOf(rs.getInt("product_id")),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getString("category")
                );
                products.add(product);
            }
        } catch (SQLException e) {
            throw new InventoryException("Database error while retrieving all products: " + e.getMessage(), e);
        }

        return products;
    }
}