FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/TeamUp-*.jar app.jar

# Create uploads directory
RUN mkdir -p /app/uploads

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

