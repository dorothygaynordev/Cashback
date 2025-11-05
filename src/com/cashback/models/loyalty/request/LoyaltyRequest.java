package com.cashback.models.loyalty.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO para solicitud de rewards por orden
 */
public class LoyaltyRequest {
    
    @JsonProperty("interface")
    private String interfaceName;
    
    @JsonProperty("storeId")
    private String storeId;
    
    @JsonProperty("checkoutId")
    private String checkoutId;
    
    @JsonProperty("client")
    private Client client;
    
    @JsonProperty("attendants")
    private Attendants attendants;
    
    @JsonProperty("items")
    private List<Item> items;
    
    @JsonProperty("shipping")
    private List<Shipping> shipping;

    // Constructores
    public LoyaltyRequest() {
        this.interfaceName = "pos";
    }
    
    public LoyaltyRequest(String storeId, List<Item> items) {
        this();
        this.storeId = storeId;
        this.items = items;
    }

    // Getters y Setters
    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getCheckoutId() {
        return checkoutId;
    }

    public void setCheckoutId(String checkoutId) {
        this.checkoutId = checkoutId;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Attendants getAttendants() {
        return attendants;
    }

    public void setAttendants(Attendants attendants) {
        this.attendants = attendants;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<Shipping> getShipping() {
        return shipping;
    }

    public void setShipping(List<Shipping> shipping) {
        this.shipping = shipping;
    }

    @Override
    public String toString() {
        return String.format(
                "LoyaltyRequest{storeId='%s', items=%d, client=%s}",
                storeId, items != null ? items.size() : 0, client != null ? "presente" : "ausente"
        );
    }
}
