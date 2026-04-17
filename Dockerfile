# ==========================================
# STAGE 1: Build (The heavy lifting)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Step 1: Copy ONLY the pom.xml first.
COPY pom.xml .

# Step 2: Download dependencies.
# Docker caches this layer! If you don't change your pom.xml,
# Docker skips this slow download step on future builds.
RUN mvn dependency:go-offline

# Step 3: Copy the actual source code.
COPY src ./src

# Step 4: Compile the application into a .jar file
RUN mvn clean package -DskipTests

# ==========================================
# STAGE 2: Run (The lightweight production image)
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy ONLY the compiled .jar file from Stage 1.
# This leaves all the heavy Maven build tools behind, keeping your image tiny.
COPY --from=builder /app/target/*.jar app.jar

# Document the port
EXPOSE 8080

# Boot the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]