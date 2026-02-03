# שלב 1: בנייה עם תמונת Maven מעודכנת
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# שלב 2: הרצת השרת עם Eclipse Temurin (המחליף הרשמי והיציב)
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/ashcollege.jar app.jar
EXPOSE 5000
ENTRYPOINT ["java","-jar","app.jar"]