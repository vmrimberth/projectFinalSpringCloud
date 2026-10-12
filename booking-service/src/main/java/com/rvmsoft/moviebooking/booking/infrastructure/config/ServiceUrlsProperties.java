package com.rvmsoft.moviebooking.booking.infrastructure.config;

//import lombok.Getter;
//import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

//@Getter
//@Setter

@ConfigurationProperties(prefix = "services")
public class ServiceUrlsProperties {

    private ServiceUrl movieService;//order-service

    public ServiceUrl getMovieService() {
        return movieService;
    }

    public void setMovieService(ServiceUrl movieService) {
        this.movieService = movieService;
    }
}
