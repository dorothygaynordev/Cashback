package com.cashback.models.loyalty.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ThisOrder {
    
    @JsonProperty("redeemable")
    private CashbackAmount redeemable;
    
    @JsonProperty("accumulating")
    private Accumulating accumulating;

    public ThisOrder() {}

    public CashbackAmount getRedeemable() { return redeemable; }
    public void setRedeemable(CashbackAmount redeemable) { this.redeemable = redeemable; }
    
    public Accumulating getAccumulating() { return accumulating; }
    public void setAccumulating(Accumulating accumulating) { this.accumulating = accumulating; }

    @Override
    public String toString() {
        return String.format(
                "ThisOrder{redeemable=%s, accumulating=%s}",
                redeemable != null ? redeemable.toString() : "null",
                accumulating != null ? accumulating.toString() : "null"
        );
    }
}