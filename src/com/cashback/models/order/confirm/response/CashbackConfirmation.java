package com.cashback.models.order.confirm.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CashbackConfirmation {
    @JsonProperty("accumulatedOrder")
    private Integer accumulatedOrder;

    // Constructores
    public CashbackConfirmation() {}

    // Getters y Setters
    public Integer getAccumulatedOrder() {
        return accumulatedOrder;
    }

    public void setAccumulatedOrder(Integer accumulatedOrder) {
        this.accumulatedOrder = accumulatedOrder;
    }

    @Override
    public String toString() {
        return String.format("CashbackConfirmation{accumulatedOrder=%d}", accumulatedOrder);
    }
}
