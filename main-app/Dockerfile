FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/talenthub.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
