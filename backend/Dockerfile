# ---- Build Stage ----
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY gradlew ./
COPY gradle/wrapper/ ./gradle/wrapper/

RUN chmod +x gradlew

COPY build.gradle settings.gradle ./

RUN ./gradlew dependencies --no-daemon 

COPY src/ ./src/

# Build the application without running tests
RUN ./gradlew clean build -x test --no-daemon

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]