# Multi-stage Dockerfile for PokerHole Server
# Stage 1: Build stage
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace/app

# Copy Gradle wrapper and configuration files
COPY gradle gradle
COPY gradlew .
COPY gradle.properties .
COPY settings.gradle .
COPY build.gradle.kts .

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build application (skip tests for faster builds)
RUN ./gradlew bootJar --no-daemon -x test

# Extract JAR layers for better caching
RUN mkdir -p build/dependency && \
    cd build/dependency && \
    java -Djarmode=layertools -jar ../libs/*.jar extract

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre

# Install curl for health checks
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r pokerhole && useradd -r -g pokerhole pokerhole

WORKDIR /app

# Copy extracted layers from builder
COPY --from=builder /workspace/app/build/dependency/dependencies/ ./
COPY --from=builder /workspace/app/build/dependency/spring-boot-loader/ ./
COPY --from=builder /workspace/app/build/dependency/snapshot-dependencies/ ./
COPY --from=builder /workspace/app/build/dependency/application/ ./

# Change ownership to non-root user
RUN chown -R pokerhole:pokerhole /app

# Switch to non-root user
USER pokerhole

# Expose application port
EXPOSE 8080

# Health check using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseStringDeduplication"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
