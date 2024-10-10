FROM amd64/amazoncorretto:17

WORKDIR /app

COPY COPY ./build/libs/dev-0.0.1-SNAPSHOT.jar /app/notify.jar

CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "-Dspring.profiles.active=dev", "/app/notify.jar"]
