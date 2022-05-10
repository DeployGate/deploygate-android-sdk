#!/usr/bin/env bash

set -euo pipefail

./gradlew clean \
    sdk:verifyBytecodeVersionRelease sdkMock:verifyBytecodeVersionRelease \
    sdk:publishReleasePublicationToMavenRepository sdkMock:publishReleasePublicationToMavenRepository \
    --stacktrace