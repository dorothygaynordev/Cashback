package com.cashback.models.loyalty.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para información del cliente en loyalty
 */
public class Client {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("countryCallingCode")
    private String countryCallingCode;
    
    @JsonProperty("cellphone")
    private String cellphone;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("name")
    private String name;

    // Constructores
    public Client() {
    }
    
    public Client(String cellphone) {
        this.cellphone = cellphone;
    }
    
    public Client(String cellphone, String name, String email) {
        this.cellphone = cellphone;
        this.name = name;
        this.email = email;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCountryCallingCode() {
        return countryCallingCode;
    }

    public void setCountryCallingCode(String countryCallingCode) {
        this.countryCallingCode = countryCallingCode;
    }

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format(
                "Client{cellphone='%s', name='%s', email='%s'}",
                cellphone, name, email
        );
    }
}
