package com.cashback.models.order.confirm.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Client {
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("countryCallingCode")
    private Integer countryCallingCode;
    
    @JsonProperty("cellphone")
    private String cellphone;
    
    @JsonProperty("document")
    private Object document;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Integer getCountryCallingCode() { return countryCallingCode; }
    public void setCountryCallingCode(Integer countryCallingCode) { this.countryCallingCode = countryCallingCode; }
    
    public String getCellphone() { return cellphone; }
    public void setCellphone(String cellphone) { this.cellphone = cellphone; }
    
    public Object getDocument() { return document; }
    public void setDocument(Object document) { this.document = document; }
}
