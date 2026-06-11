package com.example.shopping_back.chat.dto;

public record AiBargainSuggestion(String content, String source) {
    public String sourceLabel() {
        return "ai".equals(source) ? "真实 AI 模型" : "本地兜底建议";
    }
}
