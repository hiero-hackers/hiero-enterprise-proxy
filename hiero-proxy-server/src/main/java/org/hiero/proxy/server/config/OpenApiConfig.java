package org.hiero.proxy.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Provider;
import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Local Development Environment");

        return new OpenAPI()
                .info(new Info()
                        .title("Hiero Enterprise Proxy API")
                        .version("v1.0.0")
                        .description("Core API specification for managing account infrastructure and transaction proxy flows.")
                        .contact(new Contact()
                                .name("Engineering Team")))
                .servers(List.of(localServer));

    }
}
