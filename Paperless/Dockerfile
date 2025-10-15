# Basis-Image Java 21
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/paperless-0.0.1-SNAPSHOT.jar paperless.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "paperless.jar"]