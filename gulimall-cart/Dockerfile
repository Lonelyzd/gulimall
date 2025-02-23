FROM openjdk:8-alpine3.7
EXPOSE 8080

VOLUME /tmp
ADD target/*.jar  /app.jar
RUN  'touch /app.jar'
ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=prod"]
