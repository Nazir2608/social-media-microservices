package com.socialmedia.post.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Social Media Post Service",
                version = "0.0.1-SNAPSHOT",
                description = "Post management APIs for the Social Media application. "
                        + "Provides endpoints to upload media posts, manage captions and hashtags, "
                        + "and emit post_created events to Kafka."
        ),
        servers = {
                @Server(url = "http://localhost:8081", description = "Local Post Service")
        }
)
public class OpenApiConfig {
}

