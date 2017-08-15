#!/usr/bin/env bash
./gradlew clean assembleRelease bintrayUpload -PbintrayUser=$BINTRAY_USER -PbintrayKey=$BINTRAY_KEY -PdryRun=false --stacktrace
