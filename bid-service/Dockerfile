FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .

# собираем нужный сервис + зависимости
RUN mvn clean package -pl bid-service -am -DskipTests

FROM eclipse-temurin:17-jre
RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/bid-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]



