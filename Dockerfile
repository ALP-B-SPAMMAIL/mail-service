# 우분투 기반의 이미지 사용
FROM --platform=linux/amd64 gradle:8.13.0-jdk17 AS build

WORKDIR /app

# 프로젝트 파일 복사
COPY . /app

# Gradle 빌드 실행
RUN ./gradlew clean bootJar --no-daemon --stacktrace

# 최종 실행 이미지
FROM openjdk:17-jdk-slim

# 빌드한 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar /app/app.jar

# 9. 카프카 설정 (환경 변수 설정)
ENV KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# 앱 실행 명령
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# 포트 설정
EXPOSE 8080