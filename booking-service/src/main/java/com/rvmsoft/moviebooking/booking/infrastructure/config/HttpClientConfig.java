package com.rvmsoft.moviebooking.booking.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {

    @Bean
    public RestClient.Builder restClient() {
        return RestClient.builder();
    }
}
