# Stage 1: Build application with Maven and Java 17
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run application with JRE 17 as a non-root user
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN groupadd --system nexusmarket \
    && useradd --system --gid nexusmarket --home-dir /app nexusmarket

COPY --from=build /app/target/*.jar app.jar
RUN chown -R nexusmarket:nexusmarket /app

USER nexusmarket
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
