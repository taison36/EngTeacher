FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN ./gradlew dependencies --no-daemon || true

COPY src src

EXPOSE 8080

CMD ["./gradlew", "bootRun"]
