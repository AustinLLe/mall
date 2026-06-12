package com.example.shopping_back.chat.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Data
@Getter
@Setter
public class ChatMessageDto {
    private Integer covId;
    private String content;
    private Integer senderId;
    private String type;
    private BigDecimal priceValue; 
}
