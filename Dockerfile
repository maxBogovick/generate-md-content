# Build stage
FROM maven:3.8.3-openjdk-17 AS build
RUN mkdir /app
WORKDIR /app
COPY . /app
RUN mvn package -DskipTests -Pproduction

# Run stage
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar ./app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]