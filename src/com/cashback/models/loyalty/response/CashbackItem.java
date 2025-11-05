package com.cashback.models.loyalty.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CashbackItem {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("skuId")
    private String skuId;
    
    @JsonProperty("value")
    private Object value; // Puede ser Integer o String

    public CashbackItem() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getSkuId() { return skuId; }
    public void setSkuId(String skuId) { this.skuId = skuId; }
    
    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }

    // Método para obtener el valor como entero
    public Integer getValueAsInteger() {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return String.format("CashbackItem{id='%s', skuId='%s', value=%s}", id, skuId, value);
    }
}