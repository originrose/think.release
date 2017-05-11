#!/bin/bash

set -e

git pull

lein do clean, check, test

CURRENT_VERSION=`lein release show-current-version`

RELEASE_VERSION=`lein release show-release-version`

##guards against pushing tags for things we haven't deployed or that failed deployment.

if [ $CURRENT_VERSION != $RELEASE_VERSION ]; then

   echo "Setting release version $RELEASE_VERSION"
   lein release set-release-version

   git commit -am "Release $RELEASE_VERSION"
   git tag -a v$RELEASE_VERSION -m "Release $RELEASE_VERSION"

   lein deploy

   git push
   git push --tags origin

fi

lein release set-snapshot-version

git commit -am "Bump back to snapshot"

git push
