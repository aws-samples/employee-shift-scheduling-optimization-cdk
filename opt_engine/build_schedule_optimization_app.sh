#!/bin/bash

if [[ "$OSTYPE" == "darwin"* ]]; then
    # for macos
    export JAVA_HOME=$(/usr/libexec/java_home)
fi

# build solution
./mvnw package

# post build
mkdir -p ./dist/schedule-optimization-app
cp apps/schedule-optimization-app/target/opt-engine-runner.jar dist/schedule-optimization-app/
cp apps/schedule-optimization-app/src/main/resources/solver-config.xml dist/schedule-optimization-app/
cp scripts/dockerfile/Dockerfile.schedule_optimization_app dist/schedule-optimization-app/Dockerfile