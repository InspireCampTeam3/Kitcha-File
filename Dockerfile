# Build part
FROM gradle:8.12.1-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle build -x test --no-daemon

# Execute part
FROM amazoncorretto:17
WORKDIR /app
COPY --from=builder /app/build/libs/file-0.0.1-SNAPSHOT.jar .
ENTRYPOINT ["java", "-jar", "file-0.0.1-SNAPSHOT.jar"]
