package com.example.shopping_back.goods.model;

import java.math.BigDecimal;
import java.util.Date;

public class Goods {
    private Integer goodsId;
    private Integer sellerId;
    private String goodsName;       // 对应前端 form.title
    private String category;        // 新增：对应前端 form.category
    private String goodsDesc;       // 对应前端 form.desc
    private String goodsCondition;  // 新增：对应前端 form.condition (注意避免与Java关键字condition冲突，故用goodsCondition)
    private String story;           // 新增：对应前端 form.story
    private BigDecimal price;       // 对应前端 form.price
    private BigDecimal floorPrice;  // 新增：对应前端 form.floorPrice
    private String scene;           // 对应前端 form.scene
    private String address;         // 对应前端 form.location
    private String image;           // 对应前端 form.images (前端是数组，后端建议用 String 接收，用逗号或者JSON序列化存储)
    private String status;          // 状态：0在售，1下架，2待审核
    private Date createTime;

    // 无参构造
    public Goods() {
    }

    // 全参构造（方便往后快捷 New 对象）
    public Goods(Integer goodsId, Integer sellerId, String goodsName, String category, String goodsDesc, 
                 String goodsCondition, String story, BigDecimal price, BigDecimal floorPrice, 
                 String scene, String address, String image, String status, Date createTime) {
        this.goodsId = goodsId;
        this.sellerId = sellerId;
        this.goodsName = goodsName;
        this.category = category;
        this.goodsDesc = goodsDesc;
        this.goodsCondition = goodsCondition;
        this.story = story;
        this.price = price;
        this.floorPrice = floorPrice;
        this.scene = scene;
        this.address = address;
        this.image = image;
        this.status = status;
        this.createTime = createTime;
    }

    // ==================== Getters and Setters ====================

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public Integer getSellerId() {
        return sellerId;
    }

    public void setSellerId(Integer sellerId) {
        this.sellerId = sellerId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getGoodsDesc() {
        return goodsDesc;
    }

    public void setGoodsDesc(String goodsDesc) {
        this.goodsDesc = goodsDesc;
    }

    public String getGoodsCondition() {
        return goodsCondition;
    }

    public void setGoodsCondition(String goodsCondition) {
        this.goodsCondition = goodsCondition;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getFloorPrice() {
        return floorPrice;
    }

    public void setFloorPrice(BigDecimal floorPrice) {
        this.floorPrice = floorPrice;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    // 重写 toString，方便在 Controller 或 Service 里打 Log 调试
    @Override
    public String toString() {
        return "Goods{" +
                "goodsId=" + goodsId +
                ", sellerId=" + sellerId +
                ", goodsName='" + goodsName + '\'' +
                ", category='" + category + '\'' +
                ", goodsDesc='" + goodsDesc + '\'' +
                ", goodsCondition='" + goodsCondition + '\'' +
                ", story='" + story + '\'' +
                ", price=" + price +
                ", floorPrice=" + floorPrice +
                ", scene='" + scene + '\'' +
                ", address='" + address + '\'' +
                ", image='" + image + '\'' +
                ", status='" + status + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}