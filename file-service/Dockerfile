FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .

COPY discovery-server ./discovery-server
COPY config-server ./config-server
COPY api-gateway ./api-gateway
COPY user-service ./user-service
COPY category-service ./category-service
COPY lot-service ./lot-service
COPY bid-service ./bid-service
COPY file-service ./file-service
COPY common-events ./common-events
COPY notification-service ./notification-service

RUN mvn dependency:go-offline -B
RUN mvn clean package -DskipTests -pl file-service -am

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/file-service/target/*.jar app.jar
RUN mkdir -p /app/files
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

