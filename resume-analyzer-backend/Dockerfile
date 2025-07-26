# Use Maven to build the app
FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests

# Use OpenJDK to run the app
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Expose port (default Spring Boot port)
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
