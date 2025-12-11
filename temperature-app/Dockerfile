# Этап 1: Сборка
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Этап 2: Запуск
FROM eclipse-temurin:17-jdk
WORKDIR /app
# Копируем собранный JAR из этапа сборки
COPY --from=builder /app/target/*.jar app.jar

# Опциональные параметры Java
ENV JAVA_OPTS=""

# Запуск приложения
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
