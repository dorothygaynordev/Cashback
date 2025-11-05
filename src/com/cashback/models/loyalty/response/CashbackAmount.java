package com.cashback.models.loyalty.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class CashbackAmount {
    
    @JsonProperty("total")
    private Integer total;
    
    @JsonProperty("item")
    private List<CashbackItem> items;

    public CashbackAmount() {}

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    
    public List<CashbackItem> getItems() { return items; }
    public void setItems(List<CashbackItem> items) { this.items = items; }

    @Override
    public String toString() {
        return String.format(
                "CashbackAmount{total=%d, items=%s}",
                total, items != null ? items.size() + " items" : "null"
        );
    }
}