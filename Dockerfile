# ---------- Etapa 1: compila o projeto e gera o .jar ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia primeiro o pom.xml para aproveitar o cache das dependencias entre um deploy e outro
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Etapa 2: imagem final, so com o Java e o .jar ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/filazero-api.jar app.jar

# O Render informa a porta pela variavel PORT (lida em application.properties)
EXPOSE 8080

# Limita a memoria da JVM para caber no plano gratuito do Render (512 MB)
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
