# Dockerfile
FROM gradle:7.6-jdk17 AS builder
WORKDIR /build
COPY . .
RUN ./gradlew clean build -x test --no-daemon

FROM openjdk:17-jdk-slim
WORKDIR /app

# 보안 강화
RUN apt-get update && apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/* && \
    addgroup --system spring && \
    adduser --system --group spring

COPY --from=builder /build/build/libs/*.jar app.jar
USER spring:spring
EXPOSE 8080

ENTRYPOINT ["java", "-Xmx512m", "-Dspring.profiles.active=docker", "-jar", "app.jar"]