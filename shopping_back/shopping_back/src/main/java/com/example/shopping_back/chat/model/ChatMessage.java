package com.example.shopping_back.chat.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
@Data
@Getter
@Setter
public class ChatMessage {
    private Integer cmId;
    private Integer covId;
    private Boolean isRead;
    private Integer senderId;
    private String content;
    private BigDecimal priceValue; // 方便 AI 议价抓取
    private Date createTime;
    private String type; // CHAT_MESSAGE / AI_REPLY
}