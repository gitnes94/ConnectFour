package com.example.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * One RestClient per downstream service. Base URLs are configurable so the same
 * code points at localhost locally and at cluster DNS names in Kubernetes
 * (http://authservice:9000 etc.).
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient authClient(@Value("${services.auth-url}") String url) {
        return RestClient.builder().baseUrl(url).build();
    }

    @Bean
    public RestClient userClient(@Value("${services.user-url}") String url) {
        return RestClient.builder().baseUrl(url).build();
    }

    @Bean
    public RestClient gameClient(@Value("${services.game-url}") String url) {
        return RestClient.builder().baseUrl(url).build();
    }
}
