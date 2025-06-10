# Use Eclipse Temurin JDK 17 base image
FROM eclipse-temurin:17-jdk

# Set working directory inside container
WORKDIR /app

# Copy the JAR file (adjust if your jar name is different)
COPY target/*.jar app.jar

# Expose port 8082 (update if this service uses a different port)
EXPOSE 8082

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]