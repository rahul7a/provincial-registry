FROM openjdk:11-jre-slim

# Setting default server port for spring app in container
ENV SERVER_PORT=8085

COPY app/target/phms-provincial-registry-service*.jar /usr/local/lib/phms-provincial-registry-service.jar

EXPOSE 8085
ENTRYPOINT ["java","-jar","/usr/local/lib/phms-provincial-registry-service.jar"]
