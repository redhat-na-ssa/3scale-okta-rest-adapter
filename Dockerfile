FROM registry.redhat.io/fuse7/fuse-java-openshift-rhel8:1.10-25
WORKDIR /opt
ENV PORT 8080
EXPOSE 8080
COPY target/*.jar /opt/app.jar
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
