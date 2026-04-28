FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN ./gradlew dependencies --no-daemon || true

COPY src src

RUN ./gradlew clean build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/build/libs/et-backend.jar et-backend.jar

# Expose default Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "et-backend.jar"]
