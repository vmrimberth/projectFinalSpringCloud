package com.rvmsoft.moviebooking.booking.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración OpenAPI 3.1 para bookings-service.
 * <p>
 * Demuestra dos conceptos complejos:
 * <p>
 * 1. Objetos anidados — CreateBookingsRequest.
 * springdoc genera automáticamente el schema compuesto con $ref.
 * En la UI se ve el schema completo expandido con todos los campos anidados.
 * <p>
 * 2. Máquina de estados — BookingsStatus (PENDING → CONFIRMED | CANCELLED).
 * Documentada en los @ApiResponse de los endpoints de transición:
 * PATCH /{id}/confirm → PENDING  → CONFIRMED
 * PATCH /{id}/cancel  → PENDING  → CANCELLED
 * <p>
 * UI: http://localhost:50300/api/v1/swagger-ui.html
 * Spec JSON: http://localhost:50300/api/v1/v3/api-docs
 */
@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI bookingsServiceOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Bookings Service API")
                                .description("""
                                        Servicio de gestión de Bookings, proyecto final.
                                        
                                        **Ciclo de vida de una bookings:**
                                        ```
                                        POST /bookings          → crea bookings (PENDING)
                                        PATCH /{id}/confirm   → PENDING → CONFIRMED
                                        PATCH /{id}/cancel    → PENDING → CANCELLED
                                        ```
                                        
                                        **Integración SAGA:** Este servicio es el write-side del patrón CQRS.
                                        Las Bookings confirmadas se publican al topic Kafka `bookings.events`.
                                        """)
                                .version("1.0.0")
                                .contact(
                                        new Contact()
                                                .name("Rimberth")
                                                .email("rimberth.vm@gmail.com")
                                )
                )
                .addServersItem(
                        new Server()
                                .url("http://localhost:50300/api/v1")
                                .description("Local")
                )
                .addServersItem(
                        new Server()
                                .url("http://dev.mitocode.com/api/v1")
                                .description("Dev")
                )
                .addServersItem(
                        new Server()
                                .url("http://qa.mitocode.com/api/v1")
                                .description("QA")
                )
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT RS256 — obtener con POST /api/v1/auth/login en auth-service")
                        )
                );
    }
}
