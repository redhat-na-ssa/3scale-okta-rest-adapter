FROM adoptopenjdk/openjdk8:x86_64-ubi-jdk8u292-b10-slim
WORKDIR /opt
ENV PORT 8080
ENV PORT 8443
EXPOSE 8080
EXPOSE 8443
COPY target/*.jar /opt/app.jar
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
