# Start with a base image
FROM openjdk:17-alpine3.14

# Set the working directory inside the container
WORKDIR /app

# Copy the executable jar file and other necessary files to the container
COPY target/store-0.0.1-SNAPSHOT.jar /app/store.jar

# Expose the port that the application will listen on
EXPOSE 8080

# Define the command to run the application when the container starts
CMD ["java", "-jar", "/app/store.jar"]
