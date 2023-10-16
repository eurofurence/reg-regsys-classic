FROM alpine:3

RUN apk add --no-cache openjdk17-jre-headless

COPY ./build/libs/app-0.1.0-SNAPSHOT.jar /app/regsys-classic.jar

EXPOSE 8080

USER 8877

CMD ["/usr/bin/java", "-jar", "/app/regsys-classic.jar"]
