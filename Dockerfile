FROM openjdk:8-jre

ARG JAR_FILE

COPY target/${JAR_FILE} /usr/local/bin/kaboom-stats/kaboom-stats-service.jar

ENTRYPOINT ["java","-jar","/usr/local/bin/kaboom-stats/kaboom-stats-service.jar"]