package org.hiero.proxy.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    private static final List<String> TAG_ORDER = List.of(
            "Accounts",
            "Topics"
    );

    /**
     * Desired display order of API paths in the generated OpenAPI spec.
     * The {@link #pathOrderCustomizer()} bean rebuilds the paths map in this sequence
     * so Swagger UI renders endpoints in workflow-logical order.
     */
    private static final List<String> PATH_ORDER = List.of(
            "/api/v1/accounts",
            "/api/v1/accounts/{accountId}/balance",
            "/api/v1/accounts/operator/balance",
            "/api/v1/accounts/{accountId}/info",
            "/api/v1/accounts/{accountId}/key",
            "/api/v1/accounts/{accountId}/memo",
            "/api/v1/accounts/{accountId}",
            "/api/v1/accounts/transfer",
            "/api/v1/accounts/{accountId}/transfer",
            "/api/v1/accounts/{accountId}/to/{recipientAccountId}",
            "/api/v1/topics",
            "/api/v1/topics/private",
            "/api/v1/topics/with-admin-key",
            "/api/v1/topics/private/with-admin-key",
            "/api/v1/topics/{topicId}/messages",
            "/api/v1/topics/{topicId}/messages/binary",
            "/api/v1/topics/{topicId}/messages/{sequenceNumber}",
            "/api/v1/topics/{topicId}/memo",
            "/api/v1/topics/{topicId}/admin-key",
            "/api/v1/topics/{topicId}/submit-key",
            "/api/v1/topics/{topicId}"
    );

    @Bean
    public OpenAPI customOpenAPI() {
        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Local Development Environment");

        List<Tag> orderedTags = TAG_ORDER.stream()
                .map(name -> new Tag().name(name))
                .toList();

        return new OpenAPI()
                .info(new Info()
                        .title("Hiero Enterprise Proxy API")
                        .version("v1.0.0")
                        .description("Core API specification for managing account infrastructure and transaction proxy flows.")
                        .contact(new Contact().name("Engineering Team")))
                .servers(List.of(localServer))
                .tags(orderedTags);
    }

    /**
     * Sorts the top-level tags array to match TAG_ORDER.
     * Swagger UI renders tag sections (Accounts, Topics) in this sequence.
     */
    @Bean
    public OpenApiCustomizer tagOrderCustomizer() {
        return openApi -> {
            if (openApi.getTags() == null) return;
            openApi.getTags().sort(Comparator.comparingInt(tag -> {
                int index = TAG_ORDER.indexOf(tag.getName());
                return index == -1 ? Integer.MAX_VALUE : index;
            }));
        };
    }

    /**
     * Rebuilds the paths map in PATH_ORDER sequence so the generated spec JSON
     * lists paths in the intended workflow order.
     * Combined with {@code springdoc.swagger-ui.operationsSorter=method} in
     * application.properties, this produces the correct display order in Swagger UI.
     */
    @Bean
    public OpenApiCustomizer pathOrderCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) return;

            Map<String, PathItem> originalPaths = openApi.getPaths();
            Paths reordered = new Paths();

            for (String path : PATH_ORDER) {
                if (originalPaths.containsKey(path)) {
                    reordered.addPathItem(path, originalPaths.get(path));
                }
            }

            for (Map.Entry<String, PathItem> entry : originalPaths.entrySet()) {
                if (!reordered.containsKey(entry.getKey())) {
                    reordered.addPathItem(entry.getKey(), entry.getValue());
                }
            }

            openApi.setPaths(reordered);
        };
    }
}
