FROM alpine:latest

RUN apk --no-cache add nodejs-npm=6.10.3-r1

RUN npm install -g typescript@~2.4 \
    --registry https://artifactory.palantir.build/artifactory/api/npm/all-npm/ \
    && mkdir /workdir

WORKDIR /workdir

ENTRYPOINT ["tsc"]
