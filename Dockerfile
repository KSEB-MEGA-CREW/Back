# 멀티스테이지 빌드 - 캐시 최적화, 이미지 단계별 빌드
FROM gradle:7.6-jdk17 AS cache
WORKDIR /build
# 의존성 캐시 최적화를 위해 build.gradle만 먼저 복사
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon

# 빌드 스테이지
FROM cache AS builder
# 소스코드 복사
COPY src ./src
# 빌드 실행 (캐시된 의존성 활용)
RUN ./gradlew clean build -x test --no-daemon

# 실행 스테이지 - Distroless 이미지 사용
FROM gcr.io/distroless/java17-debian11
WORKDIR /app

# 빌드된 JAR만 복사
COPY --from=builder /build/build/libs/*.jar app.jar

# Distroless는 이미 non-root 사용자 설정됨
EXPOSE 8080

# JVM 최적화 옵션
ENTRYPOINT ["java", \
           "-XX:+UseContainerSupport", \
           "-XX:MaxRAMPercentage=75.0", \
           "-XX:+UseG1GC", \
           "-XX:+UseStringDeduplication", \
           "-Dspring.profiles.active=prod", \
           "-jar", "app.jar"]