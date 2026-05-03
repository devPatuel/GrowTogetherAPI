# Stage 1: build del JAR con Gradle.
# Usamos eclipse-temurin (la distribucion oficial de OpenJDK por Eclipse)
# en su variante alpine para que la imagen base sea pequena.
FROM public.ecr.aws/docker/library/eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copiamos primero los archivos de Gradle. Si solo cambia el codigo y no
# las dependencias, Docker reutiliza esta capa cacheada y el build es mas rapido.
COPY gradle gradle
COPY gradlew build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew

# Copiamos el codigo fuente y construimos el JAR ejecutable.
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: imagen final solo con el JRE (no necesitamos el JDK para ejecutar).
FROM public.ecr.aws/docker/library/eclipse-temurin:17-jre-alpine
WORKDIR /app

# wget se usa en el HEALTHCHECK para llamar al endpoint /actuator/health.
RUN apk add --no-cache wget

# Copiamos solo el JAR producido en el stage anterior. Asi la imagen final
# pesa ~150 MB en lugar de ~500 MB.
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8081

# Docker comprueba periodicamente que la app responde en /actuator/health.
# Si falla, marca el contenedor como unhealthy y docker-compose lo reinicia.
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
    CMD wget -qO- http://localhost:8081/actuator/health || exit 1

# Activamos el profile prod por defecto. Se puede sobreescribir con
# SPRING_PROFILES_ACTIVE en docker-compose.
ENTRYPOINT ["java","-Dspring.profiles.active=prod","-jar","app.jar"]
