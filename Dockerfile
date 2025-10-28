# Use an official JDK image to build the app
FROM eclipse-temurin:21-jdk AS build

# Set working directory inside container
WORKDIR /app

# Copy everything into the container
COPY . .

# Build the application using Gradle (or Maven if applicable)
RUN ./gradlew clean build -x test

# ---- Stage 2: Run the app ----
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy the jar file from the previous build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]