package com.deploygate.plugins.internal

import com.squareup.moshi.Json

class SdkMetaData(
    @Json(name = "artifact_version")
    var artifactVersion: String,
    @Json(name = "feature_flags")
    var featureFlags: Map<String, Int>,
    @Json(name = "active_feature_flags")
    var activeFeatureFlags: Int,
    @Json(name = "meta_data")
    var metaData: Map<String, String>,
) {
    fun validate() {
        require(artifactVersion.isNotBlank())
        require(featureFlags.isNotEmpty())
        require(activeFeatureFlags > 0)
        require(metaData.isNotEmpty())
    }
}
