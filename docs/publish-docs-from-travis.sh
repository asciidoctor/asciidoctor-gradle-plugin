#!/usr/bin/env bash

set -e
set -x

$(dirname $(dirname $0))/gradlew groovydoc && \
    cd $(dirname $0) && \
    ./gradlew --console=plain --info combineDocs

if [[ "${TRAVIS_PULL_REQUEST}" == "false" -a "${TRAVIS_REPO_SLUG}" == "asciidoctor/asciidoctor-gradle-plugin" ]] ; then
     ./gradlew --console=plain --info :antora:gitPublishPush :gh-pages:gitPublishPush \
            -Dorg.ajoberstar.grgit.auth.username=$GITHUB_PUBLISH_USER \
            -Dorg.ajoberstar.grgit.auth.password=$GITHUB_PUBLISH_KEY \
            -Dorg.ajoberstar.grgit.auth.force=hardcoded
fi