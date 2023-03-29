FROM java:8u111-jre-alpine

WORKDIR /workspace

COPY ./target/library-api-*.jar ./library-api.jar

CMD ["java","-Dfile.encoding=UTF-8", "-jar", "./library-api.jar"]