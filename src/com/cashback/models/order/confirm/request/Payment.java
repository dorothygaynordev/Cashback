package com.cashback.models.order.confirm.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Payment {
    
    @JsonProperty("paymentSystem")
    private String paymentSystem;
    
    @JsonProperty("value")
    private Integer value;
    
    @JsonProperty("paymentSystemName")
    private String paymentSystemName;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("installments")
    private Integer installments;
    
    @JsonProperty("firstDigits")
    private String firstDigits;
    
    @JsonProperty("lastDigits")
    private String lastDigits;

    // Constructores
    public Payment() {}

    public Payment(String paymentSystem, Integer value, String type) {
        this.paymentSystem = paymentSystem;
        this.value = value;
        this.type = type;
    }

    // Getters y Setters
    public String getPaymentSystem() {
        return paymentSystem;
    }

    public void setPaymentSystem(String paymentSystem) {
        this.paymentSystem = paymentSystem;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getPaymentSystemName() {
        return paymentSystemName;
    }

    public void setPaymentSystemName(String paymentSystemName) {
        this.paymentSystemName = paymentSystemName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getInstallments() {
        return installments;
    }

    public void setInstallments(Integer installments) {
        this.installments = installments;
    }

    public String getFirstDigits() {
        return firstDigits;
    }

    public void setFirstDigits(String firstDigits) {
        this.firstDigits = firstDigits;
    }

    public String getLastDigits() {
        return lastDigits;
    }

    public void setLastDigits(String lastDigits) {
        this.lastDigits = lastDigits;
    }

    @Override
    public String toString() {
        return String.format(
                "Payment{system='%s', value=%d, type='%s', installments=%s}",
                paymentSystem, value, type, installments
        );
    }
}
