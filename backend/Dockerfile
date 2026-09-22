# ==============================================================================
# Multi-Stage Dockerfile for Spring Boot Backend (Lightweight & Low-Memory)
# ==============================================================================

# Stage 1: Build JAR using Maven & Temurin 17
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build application skipping tests for speed
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal Production JRE Runtime (~160MB)
FROM eclipse-temurin:17-jre-alpine AS runner
WORKDIR /app

# Create non-root system user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create uploads directory with correct permissions
RUN mkdir -p uploads && chown -R appuser:appgroup /app

# Copy JAR from build stage
COPY --from=builder /app/target/evaluator-mapping-system-*.jar app.jar

# Switch to non-root user
USER appuser

# Environment variables with sensible defaults
ENV PORT=8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+OptimizeStringConcat -Xss512k"

# Render exposes the dynamic port defined by $PORT
EXPOSE 8080

# Run Spring Boot with container-aware JVM flags
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
