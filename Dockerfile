# Gradle Cache Dependencies Stage
# This stage caches plugin/project dependencies from *.gradle.kts and gradle.properties.
# Refer https://qiita.com/tkrplus/items/044790b4054bf644890a
FROM gradle:6.7.1-jdk8 AS builder
WORKDIR /app
COPY *.gradle.kts gradle.properties /app/
RUN gradle build --quiet --parallel

# Gradle Build Stage
# This stage builds saya, and generates fat jar.
COPY src/main/ /app/src/main/
RUN gradle shadowJar --parallel

# Final Stage
FROM openjdk:8-jre-alpine

COPY --from=builder /app/build/libs/stella-all.jar /app/stella.jar

WORKDIR /app
ENTRYPOINT ["java", "-server", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "/app/stella.jar"]
