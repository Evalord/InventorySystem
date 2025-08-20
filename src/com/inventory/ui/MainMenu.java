
package com.inventory.dao;

import com.inventory.core.Product;

import java.sql.*;
        import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // 🔹 Get all products
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, name, quantity, price FROM products";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                );
                products.add(product);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching products: " + e.getMessage());
        }
        return products;
    }

    // 🔹 Get product by ID
    public Product getProductById(int id) {
        String sql = "SELECT id, name, quantity, price FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("quantity"),
                            rs.getDouble("price")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching product by ID: " + e.getMessage());
        }
        return null;
    }

    // 🔹 Add a new product
    public boolean addProduct(Product product) {
        String sql = "INSERT INTO products (name, quantity, price) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getName());
            pstmt.setInt(2, product.getQuantity());
            pstmt.setDouble(3, product.getPrice());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error adding product: " + e.getMessage());
            return false;
        }
    }

    // 🔹 Update an existing product
    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET name = ?, quantity = ?, price = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getName());
            pstmt.setInt(2, product.getQuantity());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setInt(4, product.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating product: " + e.getMessage());
            return false;
        }
    }

    // 🔹 Delete a product by ID
    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting product: " + e.getMessage());
            return false;
        }
    }
}