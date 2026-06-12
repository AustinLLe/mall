package com.example.shopping_back.shop.model;

import java.time.LocalDateTime;

public class TopicPostRecord {
    private Integer postId;
    private Integer topicId;
    private Integer userId;
    private Integer productId;
    private Integer storeId;
    private String username;
    private String content;
    private String images;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private Integer commentCount;
    private Integer liked;
    private Integer wantCount;
    private Integer collectCount;
    private Integer wanted;
    private Integer collected;

    public Integer getPostId() { return postId; }
    public void setPostId(Integer postId) { this.postId = postId; }
    public Integer getTopicId() { return topicId; }
    public void setTopicId(Integer topicId) { this.topicId = topicId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public Integer getLiked() { return liked; }
    public void setLiked(Integer liked) { this.liked = liked; }
    public Integer getWantCount() { return wantCount; }
    public void setWantCount(Integer wantCount) { this.wantCount = wantCount; }
    public Integer getCollectCount() { return collectCount; }
    public void setCollectCount(Integer collectCount) { this.collectCount = collectCount; }
    public Integer getWanted() { return wanted; }
    public void setWanted(Integer wanted) { this.wanted = wanted; }
    public Integer getCollected() { return collected; }
    public void setCollected(Integer collected) { this.collected = collected; }
}
