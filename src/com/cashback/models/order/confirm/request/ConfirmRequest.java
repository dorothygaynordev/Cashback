package com.cashback.models.order.confirm.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfirmRequest {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("amountToRedeem")
    private Integer amountToRedeem;
    
    @JsonProperty("waitSettlement")
    private Boolean waitSettlement;
    
    @JsonProperty("payments")
    private List<Payment> payments;

    // Constructores
    public ConfirmRequest() {}

    public ConfirmRequest(String id) {
        this.id = id;
        this.waitSettlement = false;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getAmountToRedeem() {
        return amountToRedeem;
    }

    public void setAmountToRedeem(Integer amountToRedeem) {
        this.amountToRedeem = amountToRedeem;
    }

    public Boolean getWaitSettlement() {
        return waitSettlement;
    }

    public void setWaitSettlement(Boolean waitSettlement) {
        this.waitSettlement = waitSettlement;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    @Override
    public String toString() {
        return String.format(
                "OrderConfirmationRequest{id='%s', amountToRedeem=%s, waitSettlement=%s, payments=%s}",
                id, amountToRedeem, waitSettlement, payments != null ? payments.size() + " payments" : "null"
        );
    }
}
