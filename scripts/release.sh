#!/bin/bash

set -e

git pull

source scripts/core-access
lein deps

CURRENT_VERSION=`lein release show-current-version`

RELEASE_VERSION=`lein release show-release-version`

if [ $CURRENT_VERSION != $RELEASE_VERSION ]; then
   
   echo "Setting release version $RELEASE_VERSION"
   lein release set-release-version

   git commit -am "Release $RELEASE_VERSION"
   git tag -a v$RELEASE_VERSION -m "Release $RELEASE_VERSION"
   
   git push
   git push --tags origin
   
   lein deploy
   
fi

scripts/build-docker.sh

lein release set-snapshot-version

git commit -am "Bump back to snapshot"

git push
