package com.example.shopping_back.chat.dto;

import java.math.BigDecimal;
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
    private String status;
    private Integer goodsId;
    private String storeId;
    private String goodsName;
    private BigDecimal goodsPrice;
    private String goodsCategory;
    private String goodsScene;
    private String goodsImageUrl;   

}
