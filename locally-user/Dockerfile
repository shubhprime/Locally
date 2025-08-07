# Multi-stage build
# Stage 1: Build the application
FROM eclipse-temurin:17-jdk AS builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for better layer caching)
COPY mvnw ./
COPY .mvn ./.mvn
COPY pom.xml ./

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests since you don't have any real tests)
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre

# Set working directory inside container
WORKDIR /app

# Copy the JAR file from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Expose port 8081
EXPOSE 8081

# Run the jar file with optimized JVM settings
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]