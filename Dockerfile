FROM gradle:8.14-jdk17

WORKDIR /app

COPY . .

RUN gradle clean bootJar --no-daemon

EXPOSE 8080

CMD ["sh", "-c", "java -jar build/libs/*.jar"]