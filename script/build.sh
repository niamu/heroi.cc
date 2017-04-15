#!/bin/sh
#
# Build the Docker image
# If running on a GitLab CI server, push the final image to registry
#

set -e

# Determine current git branch name
if [ -z "$CI_BUILD_REF_NAME" ]; then
    CI_BUILD_REF_NAME=$(git branch | grep "*" | cut -d " " -f 2)
fi

DOCKER_IMAGE=fume
VERSION=$(cat project.clj | grep "defproject" | sed -e "s/.*\"\(.*\)\"/\1/")
TAG=$CI_BUILD_REF_NAME-$VERSION

# Build the Docker image with the compiled Uberjar
docker build -t $DOCKER_IMAGE:$TAG .

# Only push the image to the registry if we are building on GitLab CI
if [ -n "$GITLAB_CI" ]; then
    docker push $DOCKER_IMAGE:$TAG

    # If we are on the master branch, tag the image as "latest"
    if [ "master" == "$CI_BUILD_REF_NAME" ]; then
        docker tag $DOCKER_IMAGE:$TAG \
               $DOCKER_IMAGE:latest
        docker push $DOCKER_IMAGE:latest
        docker rmi $DOCKER_IMAGE:latest
    fi

    # Remove images we created
    docker rmi $DOCKER_IMAGE:$TAG
    docker rmi clojure:lein-2.7.1-alpine

    # Remove dangling images that may be remaining
    while [ -n "$(docker images -f "dangling=true" -q)" ]; do
        docker rmi $(docker images -f "dangling=true" -q)
    done
fi
