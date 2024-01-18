FROM gradle:7.6.3-jdk17@sha256:2ba35ae7d01f8ece2bac77fc025dd0f04e94611d71692b55621a5bff4eeef2ba AS cache
WORKDIR /app
ENV GRADLE_USER_HOME /app/gradle
COPY *.gradle.kts gradle.properties /app/
RUN gradle shadowJar --parallel --console=verbose

FROM gradle:7.6.3-jdk17@sha256:2ba35ae7d01f8ece2bac77fc025dd0f04e94611d71692b55621a5bff4eeef2ba AS build
WORKDIR /app
COPY --from=cache /app/gradle /home/gradle/.gradle
COPY *.gradle.kts gradle.properties /app/
COPY src/jvmMain/ /app/src/jvmMain/
RUN gradle shadowJar --parallel --console=verbose

FROM amazoncorretto:21.0.2@sha256:e268835d2ba1559ef39f9c7d9fc69501ff45143e6fbf89ada118017ad14f52c1 as runtime

COPY --from=build /app/build/libs/stella-all.jar /app/stella.jar
COPY docs/ /app/docs/

LABEL org.opencontainers.image.source="https://github.com/SlashNephy/stella"
WORKDIR /app
ENTRYPOINT ["java", "-jar", "/app/stella.jar"]
