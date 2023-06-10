FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} spring-ticket-generator-new.jar
EXPOSE 8080
ENTRYPOINT ["java","-Dspring.profiles.active=dev", "-jar","/spring-ticket-generator-new.jar"]