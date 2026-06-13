package com.example.shopping_back.chat.controller;

import com.example.shopping_back.chat.service.AiAssistantService;
import com.example.shopping_back.common.dto.ApiResult;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping("/assistant")
    public ApiResult<String> askAssistant(@RequestBody Map<String, String> body) {
        String question = body.getOrDefault("question", "");
        String answer = aiAssistantService.ask(question);
        return ApiResult.ok(answer);
    }
}
