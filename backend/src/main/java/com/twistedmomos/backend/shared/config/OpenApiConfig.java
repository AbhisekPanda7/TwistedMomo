package com.twistedmomos.backend.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI twistedMomosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Twisted Momos API")
                        .version("0.1.0")
                        .description("""
                                Ordering API for the Twisted Momos storefront.

                                Most endpoints require a bearer token: call POST /api/v1/auth/login, \
                                then paste the returned accessToken into Authorize above."""))
                // Declared globally rather than per-endpoint so Swagger UI shows one
                // Authorize button and sends the header on every "Try it out".
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
