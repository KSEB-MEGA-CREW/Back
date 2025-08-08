# Dockerfile.dev
# 개발 환경용 Dockerfile (빠른 빌드)

FROM openjdk:17-jdk-slim

WORKDIR /app

# 개발용 도구 설치
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    vim \
    net-tools \
    && rm -rf /var/lib/apt/lists/*

# Gradle 설치 (선택사항 - 로컬 빌드용)
#RUN wget https://services.gradle.org/distributions/gradle-8.5-bin.zip \
#    && unzip gradle-8.5-bin.zip \
#    && mv gradle-8.5 /opt/gradle \
#    && rm gradle-8.5-bin.zip

ENV PATH="/opt/gradle/bin:${PATH}"

# 소스코드 복사 (개발 시 볼륨 마운트 사용 권장)
COPY . .

# 개발용 빌드
RUN ./gradlew build -x test --no-daemon

EXPOSE 8080

# 개발용 실행 (Hot Reload 지원)
CMD ["./gradlew", "bootRun", "--no-daemon"]