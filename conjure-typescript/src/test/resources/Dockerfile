FROM alpine:latest

RUN apk --no-cache add nodejs-npm

RUN npm install -g typescript@~2.6.1 \
    --registry https://artifactory.palantir.build/artifactory/api/npm/all-npm/ \
    && mkdir /workdir

WORKDIR /workdir

ENTRYPOINT ["tsc"]
