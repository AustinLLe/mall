package com.example.shopping_back.shop.model;

import java.time.LocalDateTime;

public class ProductReviewRecord {
    private Integer reviewId;
    private Integer orderId;
    private Integer goodsId;
    private Integer buyerId;
    private Integer sellerId;
    private Integer productScore;
    private Integer sellerScore;
    private String content;
    private LocalDateTime createdAt;
    private String buyerName;

    public Integer getReviewId() { return reviewId; }
    public void setReviewId(Integer reviewId) { this.reviewId = reviewId; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getGoodsId() { return goodsId; }
    public void setGoodsId(Integer goodsId) { this.goodsId = goodsId; }
    public Integer getBuyerId() { return buyerId; }
    public void setBuyerId(Integer buyerId) { this.buyerId = buyerId; }
    public Integer getSellerId() { return sellerId; }
    public void setSellerId(Integer sellerId) { this.sellerId = sellerId; }
    public Integer getProductScore() { return productScore; }
    public void setProductScore(Integer productScore) { this.productScore = productScore; }
    public Integer getSellerScore() { return sellerScore; }
    public void setSellerScore(Integer sellerScore) { this.sellerScore = sellerScore; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
}
