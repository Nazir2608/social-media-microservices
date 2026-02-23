package com.socialmedia.user.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Social Media User Service", version = "0.0.1-SNAPSHOT",
                description = "User management APIs for the Social Media application. "
                        + "Provides endpoints for registration, login, profile retrieval, and profile updates such as bio."),
        servers = {@Server(url = "http://localhost:8080", description = "Local User Service")})
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer",bearerFormat = "JWT")
public class OpenApiConfig {
}
