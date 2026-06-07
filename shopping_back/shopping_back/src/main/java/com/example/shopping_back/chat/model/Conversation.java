package com.example.shopping_back.chat.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class Conversation {
    private Integer covId;
    private Integer buyerId;
    private Integer sellerId;
    private Integer goodsId;
    private String status; // 议价状态
    private Date createTime;
    private Date updateTime;

}