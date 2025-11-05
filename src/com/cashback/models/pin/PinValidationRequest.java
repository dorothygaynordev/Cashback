package com.cashback.models.pin;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PinValidationRequest {
     @JsonProperty("pin")
    private String pin;

    // Constructores
    public PinValidationRequest() {}

    public PinValidationRequest(String pin) {
        this.pin = pin;
    }

    // Getters y Setters
    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
