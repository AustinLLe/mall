package com.example.shopping_back.chat.dto;

import java.math.BigDecimal;
public class ChatMessageDto {
    private Integer covId;
    private String content;
    private Integer senderId;
    private String type;
    private BigDecimal priceValue; 
    public Integer getCovId() {
        return covId;
    }
    public void setCovId(Integer covId) {
        this.covId = covId;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Integer getSenderId() {
        return senderId;
    }
    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public BigDecimal getPriceValue() {
        return priceValue;
    }
    public void setPriceValue(BigDecimal priceValue) {
        this.priceValue = priceValue;
    }
}
