FROM openjdk:8-alpine3.7
EXPOSE 8080

VOLUME /tmp
ADD renren-fast.jar  /app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-jar","-Xms128m","-Xmx128m","/app.jar","--spring.profiles.active=prod"]
