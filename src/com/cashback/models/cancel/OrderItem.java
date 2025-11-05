package com.cashback.models.cancel;

public class OrderItem {
    private String id;
    private Integer quantity;
    private Integer price;

    // Constructors
    public OrderItem() {
    }

    public OrderItem(String id, Integer quantity, Integer price) {
        this.id = id;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}
