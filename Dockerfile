FROM openjdk:17-jdk-alpine
EXPOSE 8081
COPY build/libs/trainerService-0.0.1-SNAPSHOT.jar trainerService.jar
ENTRYPOINT ["java","-jar","/trainerService.jar"]