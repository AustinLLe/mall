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
}