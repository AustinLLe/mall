package com.example.shopping_back.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

@Configuration
public class AiConfig {

    @Value("${ai.bargain.api.key:}")
    private String apiKey;

    @Value("${ai.bargain.base-url:https://api.deepseek.com/v1}")
    private String baseUrl;

    @Value("${ai.bargain.model:deepseek-chat}")
    private String modelName;

    @Bean
    ChatLanguageModel chatModel() {
        String key = this.apiKey == null ? "" : this.apiKey.trim();
        if (key.isBlank()) {
            System.out.println("🤖 -> [AI议价] 未配置 AI_BARGAIN_API_KEY，将使用规则回退模式");
            return null;
        }
        System.out.println("🤖 -> [AI议价] 模型: " + this.modelName + ", base: " + this.baseUrl);
        return OpenAiChatModel.builder()
                .apiKey(key)
                .baseUrl(this.baseUrl == null || this.baseUrl.isBlank() ? "https://api.deepseek.com/v1" : this.baseUrl.trim())
                .timeout(Duration.ofSeconds(60))
                .modelName(this.modelName == null || this.modelName.isBlank() ? "deepseek-chat" : this.modelName.trim())
                .build();
    }
}