FROM gradle:7-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM openjdk:11
EXPOSE 8159:8159
RUN mkdir /src
COPY --from=build /home/gradle/src/build/libs/*.jar /src/ktor-docker-sample.jar
ENTRYPOINT ["java","-jar","/src/ktor-docker-sample.jar"]
