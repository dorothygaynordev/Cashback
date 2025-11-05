package com.cashback.models.cancel;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CancelOrderResponse {
    @JsonProperty("TID")
    private String tid;
    
    @JsonProperty("order")
    private Long order;
    
    @JsonProperty("reward")
    private List<Reward> reward;
    
    @JsonProperty("redemption")
    private List<Redemption> redemption;

    // Constructores
    public CancelOrderResponse() {}

    // Getters y Setters
    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public Long getOrder() {
        return order;
    }

    public void setOrder(Long order) {
        this.order = order;
    }

    public List<Reward> getReward() {
        return reward;
    }

    public void setReward(List<Reward> reward) {
        this.reward = reward;
    }

    public List<Redemption> getRedemption() {
        return redemption;
    }

    public void setRedemption(List<Redemption> redemption) {
        this.redemption = redemption;
    }
}

class Reward {
    
    @JsonProperty("cardId")
    private Long cardId;
    
    @JsonProperty("value")
    private Integer value;

    // Getters y Setters
    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}

class Redemption {
    
    @JsonProperty("cardId")
    private Long cardId;
    
    @JsonProperty("value")
    private Integer value;

    // Getters y Setters
    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
