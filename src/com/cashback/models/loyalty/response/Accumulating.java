package com.cashback.models.loyalty.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Accumulating {
    
    @JsonProperty("total")
    private Integer total;
    
    @JsonProperty("points")
    private Integer points;
    
    @JsonProperty("item")
    private List<AccumulatingItem> items;

    public Accumulating() {}

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    
    public List<AccumulatingItem> getItems() { return items; }
    public void setItems(List<AccumulatingItem> items) { this.items = items; }

    @Override
    public String toString() {
        return String.format(
                "Accumulating{total=%d, points=%d, items=%s}",
                total, points, items != null ? items.toString() : "null"
        );
    }
}