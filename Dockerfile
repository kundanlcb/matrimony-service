# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Copy gradle wrapper and related files
COPY gradlew .
COPY gradle gradle

# Ensure gradlew has execution permissions
RUN chmod +x ./gradlew

# Download dependencies first to cache them
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon || true

# Copy the actual source code and build the application
COPY src src
RUN ./gradlew bootJar --no-daemon

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Run as a non-root user for better security
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

# Copy the built jar from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Default port for Spring Boot
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
