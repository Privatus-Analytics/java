#!/bin/sh
# Runs the tests, the Spotless (google-java-format) check and the package build
# (jars, sources jars, javadoc jars) in a pinned Maven image. Usage: ./run-docker.sh
# The named volume keeps downloaded Maven packages between runs.
set -e
docker run --rm -v "$PWD":/app -v privatus-java-m2:/root/.m2 -w /app maven:3.9-eclipse-temurin-21 \
  mvn -B -ntp clean verify
