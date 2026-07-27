FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS production

WORKDIR /app

COPY --from=build /app/target/vaops-backend.jar vaops-backend.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "vaops-backend.jar"]