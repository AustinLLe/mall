package com.example.shopping_back.chat.dto;

import java.util.Date;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

// 专门给列表页看的 DTO
@Data
@Getter
@Setter
public class ConversationListDto {
    private Integer covId;
    private Integer unreadCount;
    private String targetName;         
    private String lastMessage;     
    private Date lastTime;        
    private String goodsImageUrl;
    private String goodsName;
    private Integer goodsId;

}