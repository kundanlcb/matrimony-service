FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Run as a non-root user for better security
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

# Copy the built jar from the host
COPY build/libs/*-SNAPSHOT.jar app.jar

# Default port for Spring Boot
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
