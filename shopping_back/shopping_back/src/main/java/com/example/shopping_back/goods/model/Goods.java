package com.example.shopping_back.goods.model;

import java.math.BigDecimal;
import java.util.Date;

public class Goods {
    private Integer goodsId;
    private Integer sellerId;
    private String goodsName;
    private String goodsDesc;
    private BigDecimal price; 
    private String scene;
    private String address;
    private String image;
    private String status;
    private Date createTime;

    public Goods(){
    }

    public Integer getGoodsId() {
        return goodsId;
    }
    public Integer getSellerId() {
        return sellerId;
    }
    public String getGoodsName() {
        return goodsName;
    }
    public String getGoodsDesc() {
        return goodsDesc;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public String getScene() {
        return scene;
    }
    public String getAddress() {
        return address;
    }
    public String getImage() {
        return image;
    }
    public String getStatus() {
        return status;
    }
    public Date getCreateTime() {
        return createTime;
    }
    public void setSellerId(Integer sellerId) { this.sellerId = sellerId; }
    public void setGoodsName(String goodsName) { this.goodsName = goodsName; }
    public void setGoodsDesc(String goodsDesc) { this.goodsDesc = goodsDesc; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setScene(String scene) { this.scene = scene; }
    public void setAddress(String address) { this.address = address; }
    public void setImage(String image) { this.image = image; }
    public void setStatus(String status) { this.status = status; }
    public void setGoodsId(Integer goodsId) { this.goodsId = goodsId; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
