package com.rvmsoft.moviebooking.booking.infrastructure.security.common;


import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Interceptor que propaga el JWT de la request entrante a todas las llamadas HTTP salientes.
 * <p>
 * Patrón: Token Relay / Header Propagation.
 * Funciona con: RestClient (requestInterceptor) y RestTemplate (interceptors).
 * <p>
 * Flujo:
 * 1. El api-gateway valida el JWT del cliente y lo reenvía al orchestrator
 * 2. Spring Security parsea el token → JwtAuthenticationToken en SecurityContextHolder
 * 3. Este interceptor lee el token del SecurityContextHolder
 * 4. Añade Authorization: Bearer <token> a todas las requests salientes
 * 5. El order-service y otros downstream reciben el mismo token y lo validan
 * <p>
 * Implementa ClientHttpRequestInterceptor (Spring Framework MVC).
 * Compatible con: RestClient.Builder.requestInterceptor() y RestTemplate.interceptors.
 *
 * see FeignJwtInterceptor para OpenFeign
 * see HttpClientConfig donde se registra en los builders
 */
@Component
@Profile("jwt")
public class JwtPropagationInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            // Solo añadir si no hay ya un Authorization header (no sobreescribir)

            if (request.getHeaders().get(HttpHeaders.AUTHORIZATION) == null) {
                String tokenValue = jwtToken.getToken().getTokenValue();
                request.getHeaders().setBearerAuth(tokenValue);
            }
        }
        return execution.execute(request, body);
    }
}