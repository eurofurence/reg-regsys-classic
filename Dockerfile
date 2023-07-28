FROM alpine:3 as build

ARG TOMCAT9_VERSION=9.0.78
ARG ECS_LOGGING_VERSION=1.5.0

# 1 jre headless, tar, gzip

RUN apk add --no-cache tar gzip openjdk17-jre-headless

# 2 tomcat incl. custom config

ADD https://dlcdn.apache.org/tomcat/tomcat-9/v${TOMCAT9_VERSION}/bin/apache-tomcat-${TOMCAT9_VERSION}.tar.gz /apache-tomcat-${TOMCAT9_VERSION}.tar.gz

RUN tar xzf apache-tomcat-${TOMCAT9_VERSION}.tar.gz \
    && mv apache-tomcat-${TOMCAT9_VERSION}.tar.gz tomcat \
    && rm -rf tomcat/logs \
    && rm -rf tomcat/webapps/docs \
    && rm -rf tomcat/webapps/examples \
    && rm -rf tomcat/webapps/host-manager \
    && rm -rf tomcat/webapps/manager \
    && rm -rf tomcat/webapps/ROOT \
    && rm tomcat/BUILDING.txt \
    && rm tomcat/CONTRIBUTING.md \
    && rm tomcat/RELEASE-NOTES \
    && rm tomcat/README.md \
    && rm tomcat/RUNNING.txt \
    && rm tomcat/bin/*.bat \
    && rm tomcat/bin/commons-daemon-native.tar.gz \
    && rm tomcat/bin/commons-daemon.jar \
    && rm tomcat/bin/tomcat-native.tar.gz \
    && rm tomcat/conf/tomcat-users.xml \
    && rm tomcat/conf/tomcat-users.xsd \
    && rm tomcat/temp/safeToDelete.tmp \
    && chmod -R go-w tomcat \
    && chmod 777 tomcat/temp \
    && chmod 777 tomcat/work

ADD dist/tomcat/conf/logging.properties tomcat/conf/logging.properties
ADD dist/tomcat/conf/server.xml tomcat/conf/server.xml
ADD dist/tomcat/conf/web.xml tomcat/conf/web.xml

RUN chmod 644 tomcat/conf/*

# 3 tomcat ECS json logging (java.util.logging)


# 4 regsys application

ADD build/libs/regsys-0.1.0-SNAPSHOT.war /tomcat/webapps/regsys.war


FROM alpine:3

RUN apk add --no-cache openjdk17-jre-headless

COPY --from=builder /tomcat /tomcat

ENV CATALINA_BASE=/tomcat
ENV CATALINA_HOME=/tomcat

EXPOSE 8080

USER 8877

CMD ["/tomcat/bin/catalina.sh", "run"]