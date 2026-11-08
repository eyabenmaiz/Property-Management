package com.payment.payment_service.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI paymentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Payment Service API")
                        .description("Gestion des paiements de loyer — microservice")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Payment Service Team")
                                .email("support@payment-service.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org"))
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8083")
                                .description("Accès direct"),
                        new Server()
                                .url("http://localhost:8080/payment-service")
                                .description("Via Gateway")
                ));
    }
}