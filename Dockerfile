# Build stage
FROM gradle:7.4-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon --info

# Run stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/app.jar ./app.jar
RUN ls -la  # Verify the jar exists
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]