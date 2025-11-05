package com.cashback.models.loyalty.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Cashback {
    
    @JsonProperty("thisOrder")
    private ThisOrder thisOrder;
    
    @JsonProperty("userBalance")
    private UserBalance userBalance;

    public Cashback() {}

    public ThisOrder getThisOrder() { return thisOrder; }
    public void setThisOrder(ThisOrder thisOrder) { this.thisOrder = thisOrder; }
    
    public UserBalance getUserBalance() { return userBalance; }
    public void setUserBalance(UserBalance userBalance) { this.userBalance = userBalance; }

    @Override
    public String toString() {
        return String.format(
                "Cashback{thisOrder=%s, userBalance=%s}",
                thisOrder != null ? thisOrder.toString() : "null",
                userBalance != null ? userBalance.toString() : "null"
        );
    }
}