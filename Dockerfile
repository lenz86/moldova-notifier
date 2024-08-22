FROM eclipse-temurin:19-jre-alpine

WORKDIR /embassy-notifier

COPY ./docker/entrypoint.sh entrypoint.sh
COPY ./target/moldova-notifier.jar moldova-notifier.jar
COPY ./config .

RUN chmod +x entrypoint.sh && chmod +x moldova-notifier.jar

ENTRYPOINT ["sh", "entrypoint.sh"]

EXPOSE 5000
