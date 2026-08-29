package com.example.mall.order;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class CatalogClientConfiguration {
    @Bean
    RestClient catalogRestClient(
            @Value("${app.catalog.base-url}") String baseUrl,
            @Value("${app.catalog.connect-timeout-ms:500}") int connectTimeout,
            @Value("${app.catalog.read-timeout-ms:1000}") int readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        return RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    @Bean
    RestClient userRestClient(@Value("${app.user.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(500);
        requestFactory.setReadTimeout(1000);
        return RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
    }
}
