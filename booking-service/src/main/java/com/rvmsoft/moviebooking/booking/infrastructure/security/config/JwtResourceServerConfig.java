package com.rvmsoft.moviebooking.booking.infrastructure.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Resource Server JWT para order-orchestrator-service (Spring MVC servlet).
 *
 * Activado con: -Dspring.profiles.active=dev,jwt  o  dev,keycloak
 *
 * Política de acceso:
 *   /actuator/** → público (health check, métricas)
 *   cualquier otro request → JWT válido requerido (cualquier rol)
 *
 * El orchestrator no define reglas de autorización por rol —
 * esa responsabilidad recae en el api-gateway que ya filtró por rol.
 * Aquí solo validamos que el token sea auténtico (firma RSA válida, no expirado).
 *
 * Validación JWT:
 *   spring.security.oauth2.resourceserver.jwt.jwk-set-uri → cargado desde Config Server
 *   perfil jwt: jwk-set-uri = http://localhost:50003/oauth2/jwks (auth-service)
 *   perfil keycloak: jwk-set-uri = http://localhost:8080/realms/ecommerce/protocol/openid-connect/certs
 *
 * STATELESS: sin sesiones HTTP (microservicio stateless).
 * CSRF deshabilitado: APIs REST sin formularios HTML.
 *
 * JWT propagation a downstream services:
 *   JwtPropagationInterceptor se registra en RestClient/RestTemplate (HttpClientConfig)
 *   FeignJwtInterceptor se registra automáticamente en todos los FeignClients
 *   → el mismo Bearer token viaja de gateway → orchestrator → order-service → inventory-service
 */
@Configuration
@EnableWebSecurity
@Profile("jwt")
public class JwtResourceServerConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Actuator accesible sin token (health checks del orquestador de Docker)
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("**/swagger-ui/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // Todo lo demás requiere JWT válido
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(
                        // jwk-set-uri se inyecta desde Config Server (application-jwt.yml o
                        // application-keycloak.yml) — no está hardcodeada aquí (OCP: no cambiar código
                        // al cambiar el proveedor de identidad, solo cambiar el perfil activo)
                        oauth2 -> oauth2.jwt(jwt -> {})  // valida JWT con jwk-set-uri configurado en application-jwt.yml
                )
                .build();
    }
}