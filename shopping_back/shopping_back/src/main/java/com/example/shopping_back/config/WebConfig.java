package com.example.shopping_back.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 访问 /files/** 时，去磁盘 D:/upload/ 找东西
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:D:/upload/");
    }
}
