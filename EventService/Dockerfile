FROM eclipse-temurin:23-jdk-alpine

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven

RUN mvn clean package -DskipTests

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "target/EventService-0.0.1-SNAPSHOT.jar"] 