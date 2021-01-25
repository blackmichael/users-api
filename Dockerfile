FROM openjdk:13.0.2

WORKDIR /
ADD build/libs/users-api-all.jar users-api.jar
EXPOSE 8080
CMD java -jar users-api.jar