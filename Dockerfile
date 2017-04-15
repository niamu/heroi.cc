FROM clojure:lein-2.7.1-alpine
MAINTAINER Brendon Walsh <brendonwalsh@niamu.com>

COPY . /usr/src/fume/
WORKDIR /usr/src/fume/

RUN mkdir -p resources/public/css/
RUN lein uberjar
RUN mv ./target/fume.jar .
RUN lein clean

EXPOSE 8080

CMD ["java", "-jar", "fume.jar"]
