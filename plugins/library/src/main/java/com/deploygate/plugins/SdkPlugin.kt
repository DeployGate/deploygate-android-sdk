package com.deploygate.plugins

import com.android.build.api.dsl.LibraryDefaultConfig
import com.deploygate.plugins.dsl.SdkExtension
import com.deploygate.plugins.internal.SdkExtensionImpl

class SdkPlugin : BaseSdkPlugin() {
    override fun createSdkExtension(): SdkExtension {
        return SdkExtensionImpl(
            displayName = "DeployGate SDK",
            artifactId = "sdk",
            description = "DeployGate SDK for Android",
        )
    }

    override fun LibraryDefaultConfig.configureLibraryDefaultConfig() {
        // A map of <name => isSupporting>
        val features = linkedMapOf(
            "UPDATE_MESSAGE_OF_BUILD" to true,
            "SERIALIZED_EXCEPTION" to true,
            "LOGCAT_BUNDLE" to true,
            "STREAMED_LOGCAT" to true,
            "DEVICE_CAPTURE" to true,
        )

        var flags = 0

        features.entries.forEachIndexed { i, e ->
            val (feature, isSupporting) = e

            buildConfigField("int", feature, "1 << $i")

            if (isSupporting) {
                flags = flags or 1.shl(i)
            }
        }

        addManifestPlaceholders(
            mapOf(
                "featureFlags" to flags,
                "sdkVersion" to "4",
                "sdkArtifactVersion" to artifactVersion,
            )
        )
    }
}