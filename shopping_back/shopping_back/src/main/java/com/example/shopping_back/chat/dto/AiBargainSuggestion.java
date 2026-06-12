package com.example.shopping_back.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiBargainSuggestion {
    private final String content;
    private final String source;
}
