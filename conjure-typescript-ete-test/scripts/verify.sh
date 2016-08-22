cd ../
./gradlew publishToMavenLocal
export PLUGIN_VERSION=`git describe --tags`
echo $PLUGIN_VERSION
cd -

./gradlew typescript-ete-test-api:build

cd ../
./gradlew conjure-typescript:generateTypescript "-PjavaArgs=../conjure-typescript-ete-test/typescript-ete-test-api/src/main/conjure/calculator.yml,../conjure-typescript-ete-test/typescript-ete-test-app/src/conjure"

cd -
./gradlew typescript-ete-test-server:runDev &
SERVER_PID=$!

waitforservice () {
    URL=$1
    echo "Waiting for $URL"
    while ! curl -s -k $URL
    do
        echo "$(date) - still trying $URL"
        sleep 1
    done
    echo "$(date) - connected successfully"
}

waitforservice https://localhost:8553/typescript-ete-test/api/calculator/ping
cd typescript-ete-test-app
npm run etetest
EXIT_CODE=$?

kill $!
exit $EXIT_CODE
