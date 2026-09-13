# ---------- Etapa 1: build ----------
# Imagen con Maven y JDK 21: compila el proyecto y genera el JAR.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Se copia primero solo el pom y se descargan las dependencias. Mientras el
# pom no cambie, Docker reutiliza esta capa y no vuelve a bajar todo.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# -DskipTests es necesario aquí, no solo para ir más rápido: el test de
# contexto intenta conectarse a Oracle y a Azure, que no existen durante el build.
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------- Etapa 2: runtime ----------
# Imagen liviana solo con el JRE: no lleva Maven ni el código fuente.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Zona horaria de Chile. El contenedor corre en UTC por defecto, y este servicio
# compara fechas con LocalDateTime.now() (validación de fecha futura) y guarda
# fechaCreacion: en UTC quedarían 3-4 horas adelantadas respecto a la hora local.
ENV TZ=America/Santiago

COPY --from=build /app/target/ms-vidasalud-appointments-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
