FROM gradle:7.4.2-jdk18 AS cache-server
WORKDIR /app
ENV GRADLE_USER_HOME /app/gradle

COPY *.gradle.kts gradle.properties /app/
RUN gradle shadowJar --parallel --console=verbose

FROM gradle:7.4.2-jdk18 AS build-server
WORKDIR /app

COPY --from=cache-server /app/gradle /home/gradle/.gradle
COPY *.gradle.kts gradle.properties /app/
COPY src/ /app/src/
RUN gradle shadowJar --parallel --console=verbose

FROM node:18.1.0-bullseye-slim AS cache-client
WORKDIR /app

COPY ./.yarn/ /app/.yarn/
COPY ./package.json /app/
COPY ./.yarnrc.yml /app/
COPY ./yarn.lock /app/
RUN yarn

FROM node:18.1.0-bullseye-slim AS build-client
WORKDIR /app

COPY --from=cache-client /app/node_modules/ /app/node_modules/
COPY ./ /app/
RUN yarn build

FROM amazoncorretto:18.0.1 as runtime
WORKDIR /app

COPY --from=build-client /app/out/ /app/out/
COPY --from=build-server /app/build/libs/stella-all.jar /app/stella.jar

LABEL org.opencontainers.image.source="https://github.com/SlashNephy/stella"
ENTRYPOINT ["java", "-jar", "/app/stella.jar"]
