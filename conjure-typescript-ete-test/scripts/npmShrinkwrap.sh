#!/usr/bin/env bash

### Pipeline setup. ###
## Cause bash to exit with an error on any simple command.
set -e
## Cause bash to exit with an error on any command in a pipeline.
set -o pipefail

## Get to the root project folder
cd "$(dirname "$0")"/..

cd "typescript-ete-test-app"
echo "Generating npm shrinkwrap file..."

rm -rf node_modules npm-shrinkwrap.json
npm install
npm prune
npm shrinkwrap --dev
perl -pi -e 's%https://registry.npmjs.org/%https://artifactory.palantir.build/artifactory/api/npm/all-npm/%g' npm-shrinkwrap.json
perl -pi -e 's%https://registrytwo.npmjs.org/%https://artifactory.palantir.build/artifactory/api/npm/all-npm/%g' npm-shrinkwrap.json
