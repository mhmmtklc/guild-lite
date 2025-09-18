package com.guildlite.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI guildLiteOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GuildLite API")
                        .description("Team Chat & Coin Sharing System - A modular backend for multiplayer game features")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Muhammet KILIC")
                                .email("muhammetkilic@yahoo.com")
                                .url("https://github.com/mhmmtklc"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token for authentication")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}