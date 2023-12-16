FROM gradle:7.5.1-jdk17@sha256:deb7b59256192aed1f463ab5e698c509cc77ce5d4e421d8b89b5d99eb0b24538 AS cache
WORKDIR /app
ENV GRADLE_USER_HOME /app/gradle
COPY *.gradle.kts gradle.properties /app/
RUN gradle shadowJar --parallel --console=verbose

FROM gradle:7.5.1-jdk17@sha256:deb7b59256192aed1f463ab5e698c509cc77ce5d4e421d8b89b5d99eb0b24538 AS build
WORKDIR /app
COPY --from=cache /app/gradle /home/gradle/.gradle
COPY *.gradle.kts gradle.properties /app/
COPY src/jvmMain/ /app/src/jvmMain/
RUN gradle shadowJar --parallel --console=verbose

FROM amazoncorretto:18.0.1@sha256:50da77dcfd039a3af6864d322ae3f11d25492fc91dbc575009a1073ed7319a47 as runtime

COPY --from=build /app/build/libs/stella-all.jar /app/stella.jar
COPY docs/ /app/docs/

LABEL org.opencontainers.image.source="https://github.com/SlashNephy/stella"
WORKDIR /app
ENTRYPOINT ["java", "-jar", "/app/stella.jar"]
