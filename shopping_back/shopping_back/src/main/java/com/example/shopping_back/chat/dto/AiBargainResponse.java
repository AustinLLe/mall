package com.example.shopping_back.chat.dto;

import com.example.shopping_back.chat.model.ChatMessage;

public record AiBargainResponse(ChatMessage message, String source, String sourceLabel) {
}
