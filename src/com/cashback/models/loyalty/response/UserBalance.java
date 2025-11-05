package com.cashback.models.loyalty.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserBalance {
    
    @JsonProperty("total")
    private Integer total;
    
    @JsonProperty("availableAmount")
    private Integer availableAmount;
    
    @JsonProperty("points")
    private Integer points;

    public UserBalance() {}

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    
    public Integer getAvailableAmount() { return availableAmount; }
    public void setAvailableAmount(Integer availableAmount) { this.availableAmount = availableAmount; }
    
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    @Override
    public String toString() {
        return String.format("UserBalance{total=%d, availableAmount=%d, points=%d}", 
                            total, availableAmount, points);
    }
}