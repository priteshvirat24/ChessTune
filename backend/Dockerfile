# Stage 1: Build using official Maven image (no mvnw needed)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copy the entire backend directory (pom.xml + all modules)
COPY . .
# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:21-jdk
WORKDIR /app
# Copy the built jar from the chesstune-core module to the runtime root
COPY --from=build /app/chesstune-core/target/*.jar app.jar

# Run the application
CMD ["java", "-jar", "app.jar"]
