#!/usr/bin/env bash

set -euo pipefail

./gradlew clean \
    sdk:assembleRelease sdkMock:assembleRelease \
    sdk:bintrayUpload sdkMock:bintrayUpload \
    -PbintrayUser=$BINTRAY_USER -PbintrayKey=$BINTRAY_KEY -PdryRun=false --stacktrace
