package com.example.shopping_back.config;

import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 访问 /files/** 时，去磁盘 D:/upload/ 找东西
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:D:/upload/");
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                List<MediaType> mediaTypes = new ArrayList<>(jacksonConverter.getSupportedMediaTypes());
                MediaType utf8Json = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);
                mediaTypes.remove(utf8Json);
                mediaTypes.add(0, utf8Json);
                jacksonConverter.setSupportedMediaTypes(mediaTypes);
            }
        }
    }
}
