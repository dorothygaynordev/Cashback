package com.cashback.models.loyalty.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para ítems de la orden
 */
public class Item {
    
    @JsonProperty("id")
    private String id; // SKU
    
    @JsonProperty("productId")
    private String productId;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("categoryId")
    private String categoryId;
    
    @JsonProperty("brandId")
    private String brandId;
    
    @JsonProperty("sellerId")
    private String sellerId;
    
    @JsonProperty("tax")
    private Integer tax;
    
    @JsonProperty("quantity")
    private Integer quantity;
    
    @JsonProperty("price")
    private Integer price; // en centavos
    
    @JsonProperty("listPrice")
    private Integer listPrice; // en centavos

    // Constructores
    public Item() {
    }
    
    public Item(String id, String name, Integer quantity, Integer price) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public Integer getTax() {
        return tax;
    }

    public void setTax(Integer tax) {
        this.tax = tax;
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

    public Integer getListPrice() {
        return listPrice;
    }

    public void setListPrice(Integer listPrice) {
        this.listPrice = listPrice;
    }
    
    /**
     * Obtiene el precio total del ítem (precio * cantidad)
     */
    public Integer getTotalPrice() {
        return price != null && quantity != null ? price * quantity : 0;
    }
    
    /**
     * Obtiene el precio en formato decimal
     */
    public Double getPriceDecimal() {
        return price != null ? price / 100.0 : 0.0;
    }
    
    /**
     * Obtiene el precio total en formato decimal
     */
    public Double getTotalPriceDecimal() {
        return getTotalPrice() / 100.0;
    }

    @Override
    public String toString() {
        return String.format(
                "Item{id='%s', name='%s', quantity=%d, price=%.2f}",
                id, name, quantity != null ? quantity : 0, getPriceDecimal()
        );
    }
}