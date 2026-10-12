package com.rvmsoft.moviebooking.booking.infrastructure.client;

import com.rvmsoft.moviebooking.booking.infrastructure.client.dto.ScreeningResponse;
import com.rvmsoft.moviebooking.booking.infrastructure.config.ServiceUrlsProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class MovieServiceClient {
    private final RestClient restClient;

    public MovieServiceClient(RestClient.Builder builder, ServiceUrlsProperties prop){
        this.restClient = builder
                .baseUrl(prop.getMovieService().getUrl())
                .build();
    }

    public ScreeningResponse getScreening(UUID screeningId) {
        return restClient.get()
                .uri("/screenings/{id}", screeningId)
                .retrieve()
                .body(ScreeningResponse.class);
    }

    public void reserveSeat(UUID screeningId) {
        restClient.patch()
                .uri("/screenings/{id}/reserve", screeningId)
                // Auth: AuthTokenRelayInterceptor reenvía el Authorization del request entrante
                .retrieve()
                .toBodilessEntity();
    }

    public void releaseSeat(UUID screeningId) {
        try {
            restClient.patch()
                    .uri("/screenings/{id}/release", screeningId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            // Best effort: we don't throw exception to avoid failing the SAGA cancellation
        }
    }
}
