#!/usr/bin/env bash

set -euo pipefail

die() {
  echo "$*" >&2
  exit 1
}

readonly tmp_dir="$(mktemp -d)"

readonly module_name="$1"

./gradlew \
    "${module_name}:verifyBytecodeVersionRelease" \
    "${module_name}:publishReleasePublicationToMavenLocal" \
    --stacktrace

cat "${module_name}/build/publications/release/module.json"

readonly artifact_id="$(jq -r '.component.module' "${module_name}/build/publications/release/module.json")"
readonly version="$(jq -r '.component.version' "${module_name}/build/publications/release/module.json")"

cat<<EOF > "${tmp_dir}/expected.txt"
${artifact_id}-${version}-javadoc.jar
${artifact_id}-${version}-sources.jar
${artifact_id}-${version}.aar
EOF

jq -r '[.variants[] | .files[] | .name] | unique | sort | .[]' \
  "${module_name}/build/publications/release/module.json" | tee "${tmp_dir}/actual.txt"

if ! diff "${tmp_dir}/expected.txt" "${tmp_dir}/actual.txt"; then
  die "Publications do or don't contain expected artifacts"
fi
