package com.cashback.models.loyalty.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para información de envío
 */
public class Shipping {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("courierId")
    private String courierId;
    
    @JsonProperty("courierName")
    private String courierName;
    
    @JsonProperty("price")
    private Integer price; // en centavos
    
    @JsonProperty("listPrice")
    private Integer listPrice; // en centavos

    // Constructores
    public Shipping() {
    }
    
    public Shipping(String id, String courierId, String courierName, Integer price) {
        this.id = id;
        this.courierId = courierId;
        this.courierName = courierName;
        this.price = price;
        this.listPrice = price;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourierId() {
        return courierId;
    }

    public void setCourierId(String courierId) {
        this.courierId = courierId;
    }

    public String getCourierName() {
        return courierName;
    }

    public void setCourierName(String courierName) {
        this.courierName = courierName;
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
     * Obtiene el precio en formato decimal
     */
    public Double getPriceDecimal() {
        return price != null ? price / 100.0 : 0.0;
    }

    @Override
    public String toString() {
        return String.format(
                "Shipping{id='%s', courier='%s', price=%.2f}",
                id, courierName, getPriceDecimal()
        );
    }
}
