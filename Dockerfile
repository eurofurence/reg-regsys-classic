FROM tomcat:8.5-jdk11-openjdk
LABEL maintainer="jsquirrel_github_9a6d@packetloss.de"

ADD build/libs/regsys-0.1.0-SNAPSHOT.war /usr/local/tomcat/webapps/regsys.war

EXPOSE 8080
CMD ["catalina.sh", "run"]