
# Maven + JDK 25 image so we don't depend on a committed maven wrapper.
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
# copy pom first for dependency layer caching
COPY pom.xml .
RUN mvn -B dependency:go-offline || true
# copy sources (includes src/main/proto for the gRPC services) and build
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- run stage ----
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
