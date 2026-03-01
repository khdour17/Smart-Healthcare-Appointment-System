# ══════════════ STAGE 1: BUILD ══════════════
# Uses Maven to compile the project and create a JAR file
# This stage is discarded after the JAR is built (multi-stage build)
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

# Copy pom.xml first — Docker caches this layer
# Dependencies only re-download when pom.xml changes
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the JAR (skip tests — they need DB)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ══════════════ STAGE 2: RUN ══════════════
# Uses a slim JDK image — no Maven, no source code, just the JAR
# Final image is ~300MB instead of ~800MB
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copy only the built JAR from stage 1
COPY --from=build /app/target/*.jar app.jar

# Document that the app runs on port 8080
EXPOSE 8080

# Start the Spring Boot application
# Use exec form (not shell form) so the JVM receives SIGTERM properly
ENTRYPOINT ["java", "-jar", "app.jar"]