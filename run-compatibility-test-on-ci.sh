#!/usr/bin/env bash

if [[ -z CI_JOB_NAME ]] ; then
    echo "CI_JOB_NAME not set. Exiting"
    exit 1
fi

GRADLE_VERS=${CI_JOB_NAME/test:gradle_/}

if [[ -z GRADLE_VERS ]] ; then
    echo No Gradle versions could be extracted from CI_JOB_NAME. Exiting
    exit 1
fi

export JAVA_OPTS=-XX:+UnlockExperimentalVMOptions
exec ./gradlew -i -s --console=plain --no-build-cache gradleTest \
    -DgradleTest.versions=${GRADLE_VERS}
