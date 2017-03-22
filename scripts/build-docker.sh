#!/bin/bash

set -e
script_dir=$(dirname $0)

DOCKER_VERSION=`head -n 1 project.clj | awk '{ print $3}' | sed 's/"//g'`

lein clean && lein uberjar

echo "Building docker.thinktopic.com/think.release:$DOCKER_VERSION"

docker build -t docker.thinktopic.com/think.release:$DOCKER_VERSION .
#docker push docker.thinktopic.com/think.release:$DOCKER_VERSION
