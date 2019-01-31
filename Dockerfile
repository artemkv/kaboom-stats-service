FROM openjdk:8-jre

RUN mkdir -p /usr/local/bin/kaboom-stats
RUN mkdir -p /usr/local/bin/kaboom-stats/logs

WORKDIR /usr/local/bin/kaboom-stats

ARG JAR_FILE

COPY target/${JAR_FILE} /usr/local/bin/kaboom-stats/kaboom-stats-service.jar

EXPOSE 8500

ENV SERVER_ADDRESS=0.0.0.0
ENV SERVER_PORT=8500
ENV LOGGING_PATH=/usr/local/bin/kaboom-stats/logs
ENV JAVA_OPTIONS -Xmx256m -Xms256m

ENTRYPOINT ["java", "$JAVA_OPTIONS", "-jar", "/usr/local/bin/kaboom-stats/kaboom-stats-service.jar"]