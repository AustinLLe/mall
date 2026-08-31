package com.example.mall.interaction;

import com.example.mall.common.AuthClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class InteractionClientConfiguration {

    @Bean
    @Qualifier("authRestClient")
    RestClient authRestClient(@Value("${app.auth.base-url:http://127.0.0.1:8080}") String baseUrl) {
        return restClient(baseUrl, 500, 1000);
    }

    @Bean
    AuthClient authClient(@Qualifier("authRestClient") RestClient authRestClient) {
        return new AuthClient(authRestClient);
    }

    @Bean
    @Qualifier("catalogRestClient")
    RestClient catalogRestClient(@Value("${app.catalog.base-url:http://127.0.0.1:8082}") String baseUrl) {
        return restClient(baseUrl, 500, 1000);
    }

    private static RestClient restClient(String baseUrl, int connectTimeout, int readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        String url = baseUrl == null ? "" : baseUrl.replaceAll("/+$", "");
        return RestClient.builder().baseUrl(url).requestFactory(requestFactory).build();
    }
}
