FROM openjdk:17
COPY target/*.jar spring-ticket-generator-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java","-Dspring.profiles.active=prod", "-jar","spring-ticket-generator-0.0.1-SNAPSHOT.jar"]