# Estágio 1: Builder
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline

COPY src src
RUN ./mvnw clean package -DskipTests

# Estágio 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S taskcoregroup && adduser -S taskcoreuser -G taskcoregroup
COPY --from=builder /build/target/*.jar taskcore-api.jar
RUN chown taskcoreuser:taskcoregroup taskcore-api.jar

USER taskcoreuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "taskcore-api.jar"]