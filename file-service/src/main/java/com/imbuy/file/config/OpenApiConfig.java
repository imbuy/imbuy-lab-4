package com.imbuy.file.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.BinarySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        return new OpenAPI()
                .info(new Info()
                        .title("File Service API")
                        .version("1.0.0")
                        .description("File management service API documentation"))
                .servers(List.of(new Server().url("/")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME, bearerScheme));
    }

    @Bean
    public OpenApiCustomizer multipartFileCustomizer() {
        return openApi -> {
            Schema<?> multipartFileSchema = new BinarySchema()
                    .type("string")
                    .format("binary")
                    .description("Файл для загрузки");

            openApi.getComponents().addSchemas("MultipartFile", multipartFileSchema);
        };
    }
}