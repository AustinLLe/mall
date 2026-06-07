package com.example.shopping_back.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

@Configuration
public class AiConfig {

    @Value("${dashscope.api.key}")
    private String apiKey;

    @Value("${dashscope.chat.url}")
    private String baseUrl;

    @Value("${dashscope.model}")
    private String modelName;

    @Bean
    ChatLanguageModel chatModel() {
        System.out.println("🤖 -> [AI配置中心] 正在配置通义千问模型: " + this.modelName);
        return OpenAiChatModel.builder()
                .apiKey(this.apiKey)
                .baseUrl(this.baseUrl)
                .timeout(Duration.ofSeconds(60))
                .modelName(this.modelName)
                .build();
    }
}