FROM gradle:7.3.0-jdk21 AS build

WORKDIR /app

COPY gradle /app/gradle
COPY gradlew /app/
COPY build.gradle settings.gradle /app/

RUN ./gradlew build --no-daemon

COPY src /app/src

RUN ./gradlew bootJar --no-daemon

FROM openjdk:21-jdk-slim

COPY --from=build /app/build/libs/*.jar /app/app.jar

# 9. 카프카 설정 (환경 변수 설정)
ENV KAFKA_BOOTSTRAP_SERVERS=kafka:9092

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

EXPOSE 8080
