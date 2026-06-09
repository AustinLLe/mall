package com.example.shopping_back.shop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderRecord {
    private Integer orderId;
    private Integer buyerId;
    private Integer sellerId;
    private Integer goodsId;
    private String status;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private String goodsName;
    private String goodsImage;
    private String scene;
    private String category;
    private String delivery;
    private String sellerName;
    private Integer productScore;
    private Integer sellerScore;
    private String reviewContent;
    private LocalDateTime reviewedAt;

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getBuyerId() { return buyerId; }
    public void setBuyerId(Integer buyerId) { this.buyerId = buyerId; }
    public Integer getSellerId() { return sellerId; }
    public void setSellerId(Integer sellerId) { this.sellerId = sellerId; }
    public Integer getGoodsId() { return goodsId; }
    public void setGoodsId(Integer goodsId) { this.goodsId = goodsId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getGoodsName() { return goodsName; }
    public void setGoodsName(String goodsName) { this.goodsName = goodsName; }
    public String getGoodsImage() { return goodsImage; }
    public void setGoodsImage(String goodsImage) { this.goodsImage = goodsImage; }
    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDelivery() { return delivery; }
    public void setDelivery(String delivery) { this.delivery = delivery; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public Integer getProductScore() { return productScore; }
    public void setProductScore(Integer productScore) { this.productScore = productScore; }
    public Integer getSellerScore() { return sellerScore; }
    public void setSellerScore(Integer sellerScore) { this.sellerScore = sellerScore; }
    public String getReviewContent() { return reviewContent; }
    public void setReviewContent(String reviewContent) { this.reviewContent = reviewContent; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
