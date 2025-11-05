package com.cashback.models.order.confirm.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderResponse {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("interface")
    private String interface_;
    
    @JsonProperty("store")
    private Store store;
    
    @JsonProperty("cashier")
    private Object cashier;
    
    @JsonProperty("salesperson")
    private Object salesperson;
    
    @JsonProperty("client")
    private Client client;
    
    @JsonProperty("orderStatus")
    private String orderStatus;
    
    @JsonProperty("paidValue")
    private Integer paidValue;
    
    @JsonProperty("customerTag")
    private List<Integer> customerTag;
    
    @JsonProperty("items")
    private List<OrderItemResponse> items;
    
    @JsonProperty("payments")
    private List<Object> payments;
    
    @JsonProperty("total_order")
    private Integer totalOrder;
    
    @JsonProperty("total_item")
    private Integer totalItem;
    
    @JsonProperty("total_shipping")
    private Integer totalShipping;
    
    @JsonProperty("total_taxes")
    private Integer totalTaxes;
    
    @JsonProperty("total_discounts")
    private Integer totalDiscounts;
    
    @JsonProperty("histories")
    private List<Object> histories;
    
    @JsonProperty("createdAt")
    private String createdAt;
    
    @JsonProperty("integratedAt")
    private String integratedAt;
    
    @JsonProperty("indexedAt")
    private String indexedAt;
    
    @JsonProperty("updatedAt")
    private String updatedAt;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getInterface_() { return interface_; }
    public void setInterface_(String interface_) { this.interface_ = interface_; }
    
    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }
    
    public Object getCashier() { return cashier; }
    public void setCashier(Object cashier) { this.cashier = cashier; }
    
    public Object getSalesperson() { return salesperson; }
    public void setSalesperson(Object salesperson) { this.salesperson = salesperson; }
    
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    
    public Integer getPaidValue() { return paidValue; }
    public void setPaidValue(Integer paidValue) { this.paidValue = paidValue; }
    
    public List<Integer> getCustomerTag() { return customerTag; }
    public void setCustomerTag(List<Integer> customerTag) { this.customerTag = customerTag; }
    
    public List<OrderItemResponse> getItems() { return items; }
    public void setItems(List<OrderItemResponse> items) { this.items = items; }
    
    public List<Object> getPayments() { return payments; }
    public void setPayments(List<Object> payments) { this.payments = payments; }
    
    public Integer getTotalOrder() { return totalOrder; }
    public void setTotalOrder(Integer totalOrder) { this.totalOrder = totalOrder; }
    
    public Integer getTotalItem() { return totalItem; }
    public void setTotalItem(Integer totalItem) { this.totalItem = totalItem; }
    
    public Integer getTotalShipping() { return totalShipping; }
    public void setTotalShipping(Integer totalShipping) { this.totalShipping = totalShipping; }
    
    public Integer getTotalTaxes() { return totalTaxes; }
    public void setTotalTaxes(Integer totalTaxes) { this.totalTaxes = totalTaxes; }
    
    public Integer getTotalDiscounts() { return totalDiscounts; }
    public void setTotalDiscounts(Integer totalDiscounts) { this.totalDiscounts = totalDiscounts; }
    
    public List<Object> getHistories() { return histories; }
    public void setHistories(List<Object> histories) { this.histories = histories; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getIntegratedAt() { return integratedAt; }
    public void setIntegratedAt(String integratedAt) { this.integratedAt = integratedAt; }
    
    public String getIndexedAt() { return indexedAt; }
    public void setIndexedAt(String indexedAt) { this.indexedAt = indexedAt; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
