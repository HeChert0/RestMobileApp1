# Stage 1: сборка через Maven
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
# Копируем pom и исходники
COPY pom.xml .
COPY src ./src
# Собираем приложение с плагином repackage
RUN mvn clean package -DskipTests spring-boot:repackage

# Stage 2: запуск на OpenJDK
FROM openjdk:17-jdk-slim
WORKDIR /app
# Копируем собранный JAR
COPY --from=build /app/target/*.jar app.jar
# Открываем порт приложения
EXPOSE 8081
# Точка входа
ENTRYPOINT ["java", "-jar", "app.jar"]