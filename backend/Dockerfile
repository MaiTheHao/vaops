FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/vaops-backend.jar vaops-backend.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "vaops-backend.jar"]