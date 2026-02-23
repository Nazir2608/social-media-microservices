package com.socialmedia.search.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Social Media Search Service",
                version = "0.0.1-SNAPSHOT",
                description = "Search APIs for the Social Media application. "
                        + "Provides endpoints to search posts by text and hashtags using Elasticsearch."
        ),
        servers = {
                @Server(url = "http://localhost:8085", description = "Local Search Service")
        }
)
public class OpenApiConfig {
}

