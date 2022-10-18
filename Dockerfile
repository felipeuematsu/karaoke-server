FROM gradle:7-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build -x test --no-daemon

FROM openjdk:11
RUN apt-get update -y
RUN apt-get upgrade -y
RUN apt-get install -y sqlite3 libsqlite3-dev
EXPOSE 8159:8159
RUN mkdir /src
COPY --from=build /home/gradle/src/build/distributions/*.zip /src/app.zip
RUN unzip /src/app.zip -d /src
RUN mkdir /src/app
RUN mv /src/*/* /src/app
RUN rm -rf /src/app.zip
RUN rm -rf /src/app/bin/*.bat

RUN mv /src/app/bin/* /src/app/bin/app
RUN sqlite3 /src/app/bin/karaoke.sqlite
ENTRYPOINT ["sh", "/src/app/bin/app"]
