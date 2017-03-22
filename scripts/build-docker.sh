#!/bin/bash

set -e

DOCKER_VERSION=`lein release show-current-version`

lein clean && lein uberjar

echo "Building docker.thinktopic.com/think.release:$DOCKER_VERSION"

docker build -t docker.thinktopic.com/think.release:$DOCKER_VERSION .
docker push docker.thinktopic.com/think.release:$DOCKER_VERSION
