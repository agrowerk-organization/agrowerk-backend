FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

RUN addgroup -g 1001 -S builder && \
    adduser -u 1001 -S builder -G builder && \
    chown -R builder:builder /app

USER builder

COPY --chown=builder:builder gradlew .
COPY --chown=builder:builder gradle ./gradle
COPY --chown=builder:builder build.gradle .
COPY --chown=builder:builder settings.gradle .

RUN chmod +x gradlew && \
    ./gradlew dependencies --no-daemon

COPY --chown=builder:builder src ./src
RUN ./gradlew bootJar -x test --no-daemon && \
    mv build/libs/*.jar build/libs/app.jar

FROM gcr.io/distroless/java21-debian12:nonroot AS runtime

COPY --from=build --chown=nonroot:nonroot /app/build/libs/app.jar /app/app.jar

WORKDIR /app

EXPOSE 8080

ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "/app/app.jar"]