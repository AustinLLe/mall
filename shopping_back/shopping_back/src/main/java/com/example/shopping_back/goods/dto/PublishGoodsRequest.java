package com.example.shopping_back.goods.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PublishGoodsRequest {
    @NotBlank(message = "商品名称不能为空")
    private String goodsName;

    private String goodsDesc;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;
    
    private String scene;
    private String address;
    private String image;
    private String category;        // 新增：商品分类（例如：数码影音 / 家居生活）
    private String goodsCondition;  // 新增：成色/状态（例如：全新 / 9 成新）
    private String story;           // 新增：二手故事 / 新品卖点
    private BigDecimal floorPrice;  // 新增：最低可接受价（用于 AI 议价）

    public String getGoodsName() { return goodsName; }
    public void setGoodsName(String goodsName) { this.goodsName = goodsName; }
    public String getGoodsDesc() { return goodsDesc; }
    public void setGoodsDesc(String goodsDesc) { this.goodsDesc = goodsDesc; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getGoodsCondition() { return goodsCondition; }
    public void setGoodsCondition(String goodsCondition) { this.goodsCondition = goodsCondition; }
    public String getStory() { return story; }
    public void setStory(String story) { this.story = story; }
    public BigDecimal getFloorPrice() { return floorPrice; }
    public void setFloorPrice(BigDecimal floorPrice) { this.floorPrice = floorPrice; }
}