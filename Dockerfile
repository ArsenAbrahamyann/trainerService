FROM openjdk:17-jdk-alpine

# Create a user and group
RUN addgroup -S spring && adduser -S spring -G spring

# Expose port 8081
EXPOSE 8081

# Copy the jar file and change ownership to the spring user
COPY build/libs/trainerService-0.0.1-SNAPSHOT.jar /app/trainerService.jar
RUN chown spring:spring /app/trainerService.jar

# Change to the spring user
USER spring

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app/trainerService.jar"]