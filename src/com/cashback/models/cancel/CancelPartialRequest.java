package com.cashback.models.cancel;

import java.util.List;

public class CancelPartialRequest {
    private List<OrderItem> items;

    // Constructors
    public CancelPartialRequest() {
    }

    public CancelPartialRequest(List<OrderItem> items) {
        this.items = items;
    }

    // Getters and Setters
    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
