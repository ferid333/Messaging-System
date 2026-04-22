package com.messaging.chat.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Chat API",
                version = "v1",
                description = "REST API for chat conversations and attachment uploads."
        )
)
public class OpenApiConfig {
}
