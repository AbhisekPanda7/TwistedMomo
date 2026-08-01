# Multi-stage: the JDK and the Maven cache never reach the runtime image.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy the POM alone first so `mvn dependency:go-offline` is cached as its own
# layer — source edits then rebuild without re-downloading the world.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Non-root: the JVM needs nothing privileged, and /app/uploads is the only
# writable path required.
RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=build /build/target/backend.jar app.jar

# Matches app.upload.dir. Bound to a named volume in compose so images
# uploaded through the admin UI survive `docker compose down`.
RUN mkdir -p /app/uploads && chown -R spring:spring /app

USER spring
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
