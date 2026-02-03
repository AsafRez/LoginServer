# שלב 1: בניית הפרויקט בעזרת Maven
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# שלב 2: הרצת השרת עם Java 17 הקליל
FROM openjdk:17-jdk-slim
WORKDIR /app
# כאן אנחנו מעתיקים את ה-JAR שנוצר בשלב הקודם (לפי השם ב-pom.xml שלך)
COPY --from=build /app/target/ashcollege.jar app.jar
EXPOSE 5000
ENTRYPOINT ["java","-jar","app.jar"]