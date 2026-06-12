package com.example.shopping_back.shop.model;

import java.time.LocalDateTime;

public class TopicRecord {
    private Integer topicId;
    private String type;
    private String title;
    private String topicDesc;
    private String author;
    private String cover;
    private String tags;
    private String status;
    private LocalDateTime createdAt;
    private Integer postCount;
    private Integer likeCount;

    public Integer getTopicId() { return topicId; }
    public void setTopicId(Integer topicId) { this.topicId = topicId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTopicDesc() { return topicDesc; }
    public void setTopicDesc(String topicDesc) { this.topicDesc = topicDesc; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Integer getPostCount() { return postCount; }
    public void setPostCount(Integer postCount) { this.postCount = postCount; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
}
