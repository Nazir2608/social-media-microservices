package com.socialmedia.feed.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Social Media Feed Service",
                version = "0.0.1-SNAPSHOT",
                description = "Feed APIs for the Social Media application. "
                        + "Provides endpoints to retrieve personalized feeds based on followers."
        ),
        servers = {
                @Server(url = "http://localhost:8084", description = "Local Feed Service")
        }
)
public class OpenApiConfig {
}

