FROM maven:3.9-amazoncorretto-21 AS backend
WORKDIR /backend

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=docker

COPY --from=backend /backend/target/*.jar /app/app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]
