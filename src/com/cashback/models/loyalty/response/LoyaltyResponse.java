package com.cashback.models.loyalty.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO para la respuesta de rewards por orden - Actualizado según respuesta final
 */
public class LoyaltyResponse {
    
    @JsonProperty("organization")
    private String organization;
    
    @JsonProperty("interface")
    private String interfaceName;
    
    @JsonProperty("customerId")
    private Long customerId;
    
    @JsonProperty("cashback")
    private Cashback cashback;
    
    @JsonProperty("coupon")
    private List<Object> coupon;
    
    @JsonProperty("confirmToken")
    private String confirmToken;
    
    @JsonProperty("smartpos")
    private String smartpos;
    
    @JsonProperty("expiresAt")
    private String expiresAt;
    
    @JsonProperty("dateTime")
    private String dateTime;

    public LoyaltyResponse() {}

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }
    
    public String getInterfaceName() { return interfaceName; }
    public void setInterfaceName(String interfaceName) { this.interfaceName = interfaceName; }
    
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public Cashback getCashback() { return cashback; }
    public void setCashback(Cashback cashback) { this.cashback = cashback; }
    
    public List<Object> getCoupon() { return coupon; }
    public void setCoupon(List<Object> coupon) { this.coupon = coupon; }
    
    public String getConfirmToken() { return confirmToken; }
    public void setConfirmToken(String confirmToken) { this.confirmToken = confirmToken; }
    
    public String getSmartpos() { return smartpos; }
    public void setSmartpos(String smartpos) { this.smartpos = smartpos; }
    
    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
    
    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    @Override
    public String toString() {
        return String.format(
                "LoyaltyResponse{organization='%s', customerId=%d, cashback=%s}",
                organization, customerId, cashback != null ? cashback.toString() : "null"
        );
    }
}