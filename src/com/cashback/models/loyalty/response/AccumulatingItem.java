package com.cashback.models.loyalty.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccumulatingItem {
    
    @JsonProperty("skuId")
    private String skuId;
    
    @JsonProperty("value")
    private Object value; // Puede ser Integer o String
    
    @JsonProperty("points")
    private Integer points;

    public AccumulatingItem() {}

    public String getSkuId() { return skuId; }
    public void setSkuId(String skuId) { this.skuId = skuId; }
    
    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }
    
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

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
        return String.format("AccumulatingItem{skuId='%s', value=%s, points=%d}", skuId, value, points);
    }
}