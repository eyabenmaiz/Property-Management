package com.property.Property_Service.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Property Service API")
                        .version("1.0")
                        .description("API de gestion des propriétés immobilières")
                        .contact(new Contact()
                                .name("Property Service Team")
                                .email("support@property-service.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org"))
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Accès direct"),
                        new Server()
                                .url("http://localhost:8080/property-service")
                                .description("Via Gateway")
                ));
    }
}