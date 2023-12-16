FROM gradle:7.5.0-jdk17@sha256:8dd07704f6d4453f3159154eec16a9551a9efc687079122f46b53ec3a0c716a7 AS cache
WORKDIR /app
ENV GRADLE_USER_HOME /app/gradle
COPY *.gradle.kts gradle.properties /app/
RUN gradle shadowJar --parallel --console=verbose

FROM gradle:7.5.0-jdk17@sha256:8dd07704f6d4453f3159154eec16a9551a9efc687079122f46b53ec3a0c716a7 AS build
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
