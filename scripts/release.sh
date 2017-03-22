#!/bin/bash

set -e

#git pull

#scripts/deps.sh
source scripts/core-access

RELEASE_VERSION=`lein release show-project-version`

#lein release set-version

#git commit -am "Release $RELEASE_VERSION"
git tag -v$RELEASE_VERSION -m "Release $RELEASE_VERSION"

lein deploy

scripts/build-docker.sh

lein release set-version

git commit -am "Bump back to snapshot"

git push
git push --tags origin
