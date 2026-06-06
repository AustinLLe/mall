package com.example.shopping_back.chat.dto;

public class CreateConversationRequest {
    private Integer sellerId;
    private Integer goodsId;
    private String status; 
    public Integer getSellerId() {
        return sellerId;
    }
    public void setSellerId(Integer sellerId) {
        this.sellerId = sellerId;
    }
    public Integer getGoodsId() {
        return goodsId;
    }
    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
