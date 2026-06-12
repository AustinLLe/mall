package com.example.shopping_back.chat.dto;

import com.example.shopping_back.chat.model.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiBargainResponse {
    private final ChatMessage message;
    private final String source;
}
