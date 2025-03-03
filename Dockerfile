FROM amd64/amazoncorretto:21

WORKDIR /app

COPY ./build/libs/dev-0.0.1-SNAPSHOT.jar /app/moamoa.jar

CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "-Dspring.profiles.active=dev", "/app/moamoa.jar"]
