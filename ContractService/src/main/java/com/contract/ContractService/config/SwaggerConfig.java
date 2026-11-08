package com.contract.ContractService.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI contractServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ContractService API")
                        .description("Gestion des contrats de location — microservice")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ContractService Team")
                                .email("support@contract-service.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org"))
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8082")
                                .description("Accès direct"),
                        new Server()
                                .url("http://localhost:8080/contractservice")
                                .description("Via Gateway")
                ));
    }
}