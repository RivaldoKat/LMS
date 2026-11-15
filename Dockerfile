FROM eclipse-temurin:27-jdk AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -DskipTests dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:27-jre

WORKDIR /app
RUN useradd --system --uid 10001 appuser \
    && mkdir -p /tmp/uploads \
    && chown -R appuser:appuser /app /tmp/uploads

COPY --from=build --chown=appuser:appuser /app/target/*.jar /app/app.jar

ENV STORAGE_LOCATION=/tmp/uploads
USER appuser

ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
