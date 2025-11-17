FROM gradle:8.5-jdk21-alpine AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY src src
RUN gradle build --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

