package com.inventory.core;

import java.util.Objects;

public class Product {
    private String id;
    private String name;
    private double price;
    private int quantity;
    private  String category;


    //constructor with required fields
    public  Product(String id, String name){
        setId(id);
        setName(name);
    }

    //Full constructor
    public Product(String id, String name, double price, int quantity, String category)
    {
        this(id, name);
        setPrice(price);
        setQuantity(quantity);
        this.category = category; //Category can be null
    }

    //Getters
    public String getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public double getPrice()
    {
        return price;
    }

    public int getQuantity(){
        return quantity;
    }

    public String getCategory(){
        return category;
    }

    //Setters with validation
    public  void setId(String id){
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        this.id =id;
    }

    public void setName(String name){
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        this.name = name;
    }

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = price;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.quantity = quantity;
    }

    public void setCategory(String category) {
        this.category = category; // Category can be null
    }

    //Business logic methods

    /**
    *  increases product quantity
    * @param amount positive number to add
     */

    public void addStock(int amount){
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.quantity += amount;
    }

    /**
     *  Drecreases product quantity
     * @param amount positive number to deduct
     */

    public void removeStock(int amount){
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.quantity < amount) {
            throw new IllegalArgumentException("Insufficient stock");
        }
        this.quantity -= amount;
    }

    //Overrides
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price='" + price + '\'' +
                ", quantity='" + quantity + '\'' +
                ", category='" + category + '\'' +
                '}';
    }


}
