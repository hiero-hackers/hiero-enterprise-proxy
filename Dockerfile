# syntax=docker/dockerfile:1
# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

# Copy Maven wrapper and root POM first (layer cache: only invalidated when
# pom files change, not when source changes)
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml ./
COPY hiero-proxy-server/pom.xml hiero-proxy-server/

# Ensure the Maven wrapper is executable and has Unix line endings
# (Windows checkouts produce CRLF, which breaks the shebang on Alpine)
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Download all remaining publicly-available dependencies into the local repo
RUN ./mvnw dependency:go-offline -pl hiero-proxy-server -am -q

# Copy source and build the fat JAR
COPY hiero-proxy-server/src hiero-proxy-server/src
RUN ./mvnw package -pl hiero-proxy-server -am -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime

# Non-root user for security
RUN addgroup -S hiero && adduser -S hiero -G hiero
USER hiero

WORKDIR /app

# Copy the fat JAR from the build stage
COPY --from=builder /workspace/hiero-proxy-server/target/hiero-proxy-server-*.jar app.jar

# Expose the Spring Boot default port
EXPOSE 8080

# Health check — Spring Boot actuator is not required; we probe the Swagger UI
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health 2>/dev/null || \
      wget -qO- http://localhost:8080/swagger-ui/index.html > /dev/null 2>&1 || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
