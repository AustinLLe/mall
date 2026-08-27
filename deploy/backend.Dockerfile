FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

WORKDIR /build

COPY shopping_back/shopping_back/pom.xml ./pom.xml
COPY shopping_back/shopping_back/src ./src
RUN --mount=type=cache,target=/root/.m2 \
  mvn -B -ntp clean test package

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

COPY --from=build /build/target/shopping_back-*.jar /app/app.jar

RUN mkdir -p /app/uploads && chown -R app:app /app
USER app

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=5 \
  CMD wget -q -O - http://127.0.0.1:8080/api/products >/dev/null || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/app.jar"]