# Stage 1: Build using official Maven image (no mvnw needed)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copy the entire backend directory (pom.xml + all modules)
COPY . .
# Build the application
RUN mvn clean package -DskipTests
# Extract only the executable fat jar, avoiding -plain.jar or other artifacts
RUN find chesstune-core/target -maxdepth 1 -name "*.jar" ! -name "*-plain.jar" -exec cp {} /app.jar \;

# Stage 2: Runtime image
FROM eclipse-temurin:21-jdk
WORKDIR /app
# Copy the built jar from the builder stage root
COPY --from=build /app.jar app.jar

# Run the application
CMD ["java", "-jar", "app.jar"]
