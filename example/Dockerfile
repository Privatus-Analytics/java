# Conformance example app (Spring Boot + Thymeleaf). Build with the package
# folder as the context:
#   docker build -f example/Dockerfile .
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
# The starter from this repo's own source, not from Maven Central.
COPY pom.xml .
COPY core/ core/
COPY spring-boot-starter/ spring-boot-starter/
RUN mvn -B -ntp -q install -DskipTests -Dspotless.check.skip=true -Dmaven.javadoc.skip=true -Dmaven.source.skip=true
COPY example/ example/
RUN mvn -B -ntp -q -f example/pom.xml package

FROM eclipse-temurin:21-jre
COPY --from=build /src/example/target/example.jar /app/example.jar
EXPOSE 8080
USER 65534
CMD ["java", "-jar", "/app/example.jar"]
