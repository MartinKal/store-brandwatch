# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-alpine

# Set the working directory to /app
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . /app

# Build the application
RUN ./mvnw clean package -DskipTests

# Set the default command to run the application when the container starts
CMD ["java", "-jar", "target/store-0.0.1-SNAPSHOT.jar"]
