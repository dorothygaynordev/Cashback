package com.cashback.models.order.confirm.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfirmResponse {
    @JsonProperty("organization")
    private String organization;
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("TID")
    private String tid;
    
    @JsonProperty("person")
    private Long person;
    
    @JsonProperty("cashback")
    private CashbackConfirmation cashback;
    
    @JsonProperty("confirmedAt")
    private String confirmedAt;
    
    @JsonProperty("dateTime")
    private String dateTime;

    // Constructores
    public ConfirmResponse() {}

    // Getters y Setters
    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public Long getPerson() {
        return person;
    }

    public void setPerson(Long person) {
        this.person = person;
    }

    public CashbackConfirmation getCashback() {
        return cashback;
    }

    public void setCashback(CashbackConfirmation cashback) {
        this.cashback = cashback;
    }

    public String getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(String confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return String.format(
                "OrderConfirmationResponse{organization='%s', orderId='%s', person=%d, cashback=%s}",
                organization, orderId, person, cashback != null ? cashback.toString() : "null"
        );
    }
}
