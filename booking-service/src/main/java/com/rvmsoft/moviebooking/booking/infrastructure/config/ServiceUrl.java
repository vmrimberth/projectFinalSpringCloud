package com.rvmsoft.moviebooking.booking.infrastructure.config;

import org.springframework.context.annotation.Bean;

public class ServiceUrl {
    private String url; //services.order-service.url

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
