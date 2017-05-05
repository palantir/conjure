#!/usr/bin/env bash

cd $(dirname $0)/..
set -eux

if [ $# -ne 1 ]; then
    echo "publish.sh <scope name>"
    exit 1
fi

NPM_SCOPE=$1
NPM_API_PATH="artifactory.palantir.build/artifactory/api/npm/all-npm"

# get npm auth token
curl -sS -u${ARTIFACTORY_USERNAME}:${ARTIFACTORY_PASSWORD} "https://$NPM_API_PATH/auth/$NPM_SCOPE" >> .npmrc

# npm publish will ignore .npmrc unless it has the right permissions (because it contains secrets)
chmod 0600 .npmrc

echo "Publishing to NPM..."
npm publish ./dist
