#!/usr/bin/env bash

set -euo pipefail

./gradlew clean verifyBytecodeVersionDebug verifyBytecodeVersionRelease --continue

./gradlew clean \
    sdk:assembleRelease sdkMock:assembleRelease \
    sdk:publishToMavenRepository sdkMock:publishToMavenRepository \
    --stacktrace