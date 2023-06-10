FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} spring-ticket-generator-new.jar
ENTRYPOINT ["java","-Dspring.profiles.active=dev", "-jar","/spring-ticket-generator-new.jar"]