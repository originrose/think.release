#!/bin/bash

set -e

source scripts/core-access
lein deploy
scripts/build_docker.sh
