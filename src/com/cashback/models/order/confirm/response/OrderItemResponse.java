package com.cashback.models.order.confirm.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderItemResponse {
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("productId")
    private Object productId;
    
    @JsonProperty("productRefId")
    private Object productRefId;
    
    @JsonProperty("productName")
    private String productName;
    
    @JsonProperty("skuId")
    private String skuId;
    
    @JsonProperty("skuName")
    private String skuName;
    
    @JsonProperty("categoryId")
    private Object categoryId;
    
    @JsonProperty("brandId")
    private Object brandId;
    
    @JsonProperty("quantity")
    private Integer quantity;
    
    @JsonProperty("quantityCanceled")
    private Integer quantityCanceled;
    
    @JsonProperty("price")
    private Integer price;
    
    @JsonProperty("listPrice")
    private Object listPrice;
    
    @JsonProperty("redemption")
    private Integer redemption;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Object getProductId() { return productId; }
    public void setProductId(Object productId) { this.productId = productId; }
    
    public Object getProductRefId() { return productRefId; }
    public void setProductRefId(Object productRefId) { this.productRefId = productRefId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getSkuId() { return skuId; }
    public void setSkuId(String skuId) { this.skuId = skuId; }
    
    public String getSkuName() { return skuName; }
    public void setSkuName(String skuName) { this.skuName = skuName; }
    
    public Object getCategoryId() { return categoryId; }
    public void setCategoryId(Object categoryId) { this.categoryId = categoryId; }
    
    public Object getBrandId() { return brandId; }
    public void setBrandId(Object brandId) { this.brandId = brandId; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public Integer getQuantityCanceled() { return quantityCanceled; }
    public void setQuantityCanceled(Integer quantityCanceled) { this.quantityCanceled = quantityCanceled; }
    
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    
    public Object getListPrice() { return listPrice; }
    public void setListPrice(Object listPrice) { this.listPrice = listPrice; }
    
    public Integer getRedemption() { return redemption; }
    public void setRedemption(Integer redemption) { this.redemption = redemption; }
}
