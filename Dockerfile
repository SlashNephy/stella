FROM gradle:7.6.3-jdk17@sha256:edb18e9c85d52c50a2991f89d898f30e1486377cafdc04b3021de7126d04a335 AS cache
WORKDIR /app
ENV GRADLE_USER_HOME /app/gradle
COPY *.gradle.kts gradle.properties /app/
RUN gradle shadowJar --parallel --console=verbose

FROM gradle:7.6.3-jdk17@sha256:edb18e9c85d52c50a2991f89d898f30e1486377cafdc04b3021de7126d04a335 AS build
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
