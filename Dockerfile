FROM openjdk:17
COPY target/*.jar spring-ticket-generator-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-Dspring.profiles.active=prod", "-jar","spring-ticket-generator-0.0.1-SNAPSHOT.jar"]