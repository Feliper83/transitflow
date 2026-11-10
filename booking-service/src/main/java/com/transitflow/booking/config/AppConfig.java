package com.transitflow.booking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    @Bean
    public RestClient tripRestClient(@Value("${transitflow.trip-service-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
