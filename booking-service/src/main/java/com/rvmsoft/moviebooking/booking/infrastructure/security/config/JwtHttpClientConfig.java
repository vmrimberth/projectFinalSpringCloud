package com.rvmsoft.moviebooking.booking.infrastructure.security.config;

import com.rvmsoft.moviebooking.booking.infrastructure.security.common.JwtPropagationInterceptor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

/**
 * Registra JwtPropagationInterceptor en los clientes HTTP del orquestador.
 *
 * Usa BeanPostProcessor: se ejecuta inmediatamente después de que cada bean
 * es inicializado (postProcessAfterInitialization), ANTES de que los beans que
 * dependen de él sean creados. Esto garantiza que cuando RestClientOrderServiceClient
 * llame a builder.build(), el interceptor ya esté registrado en el builder.
 *
 * Por qué NO modificamos HttpClientConfig con @Autowired(required=false):
 *   Spring Cloud Eureka usa el @Primary RestClient.Builder para registrarse en Eureka.
 *   Si añadimos interceptores a ese bean, la resolución reactiva (@LoadBalanced) interfiere
 *   con las llamadas de registro causando BeanCurrentlyInCreationException circular.
 *
 * Solución: solo añadir el interceptor a los beans NOMBRADOS que usan los servicios:
 *   - "loadBalancedRestClientBuilder" → RestClientOrderServiceClient, PaymentClients
 *   - "loadBalancedRestTemplate"       → RestTemplateOrderServiceClient (perfil resttemplate)
 *
 * El @Primary RestClient.Builder (sin @LoadBalanced) queda intacto para Eureka.
 */
@Configuration
@Profile({"jwt"})
public class JwtHttpClientConfig {

    @Bean
    public BeanPostProcessor jwtInterceptorRegistrar() {
        JwtPropagationInterceptor interceptor = new JwtPropagationInterceptor();

        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                // Añadir solo a los builders load-balanced que usan los servicios del orquestador.
                // "restClientBuilder" (@Primary) se omite — lo usa Eureka internamente.
                if (bean instanceof RestClient.Builder builder
                        && "restClientWithLoadBalanced".equals(beanName)) {
                    builder.requestInterceptor(interceptor);
                }

                return bean;
            }
        };

    }
}