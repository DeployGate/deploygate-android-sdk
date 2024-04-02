package com.deploygate.plugins

import com.android.build.api.dsl.LibraryDefaultConfig
import com.deploygate.plugins.dsl.SdkExtension
import com.deploygate.plugins.internal.SdkExtensionImpl
import com.deploygate.plugins.tasks.GenerateMetaDataJsonTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import java.io.File

class SdkPlugin : BaseSdkPlugin() {
    companion object {
        // A map of <name => isSupporting>
        // The order of elements are important!
        private val SUPPORTED_FEATURES = arrayOf(
            "UPDATE_MESSAGE_OF_BUILD" to true,
            "SERIALIZED_EXCEPTION" to true,
            "LOGCAT_BUNDLE" to true,
            "STREAMED_LOGCAT" to true,
            "DEVICE_CAPTURE" to true,
        )

        private val FEATURE_FLAGS: LinkedHashMap<String, Int> =
            SUPPORTED_FEATURES.mapIndexed { idx, (feature, _) ->
                feature to 1.shl(idx)
            }.toMap(LinkedHashMap())

        private val ACTIVE_FEATURE_FLAGS =
            SUPPORTED_FEATURES.fold(0) { acc, (feature, isSupporting) ->
                if (isSupporting) {
                    acc or FEATURE_FLAGS.getOrElse(feature) { error("$feature is not found in FEATURE_FLAGS") }
                } else {
                    acc
                }
            }

        private const val KEY_PREFIX = "com.deploygate.sdk."

        private val META_DATA_ENTRIES =
            mapOf(
                "deploygate_sdk_feature_flags_name" to "${KEY_PREFIX}feature_flags",
                "deploygate_sdk_feature_flags_value" to "$ACTIVE_FEATURE_FLAGS",

                "deploygate_sdk_version_name" to "${KEY_PREFIX}version",
                "deploygate_sdk_version_value" to "4",

                "deploygate_sdk_artifact_version_name" to "${KEY_PREFIX}artifact_version",
                "deploygate_sdk_artifact_version_value" to ARTIFACT_VERSION,
            )
    }

    override fun apply(target: Project) {
        super.apply(target)

        target.tasks.register<GenerateMetaDataJsonTask>("generateMetaDataJson") {
            getArtifactVersion().set(ARTIFACT_VERSION)
            getFeatureFlags().set(FEATURE_FLAGS)
            getActiveFeatureFlags().set(ACTIVE_FEATURE_FLAGS)
            getMetaData().set(META_DATA_ENTRIES)
            outputFile =
                File(target.buildDir, "generated-sdk-metadata/sdk-meta-data-$ARTIFACT_VERSION.json")
        }
    }

    override fun createSdkExtension(): SdkExtension {
        return SdkExtensionImpl(
            displayName = "DeployGate SDK",
            artifactId = "sdk",
            description = "DeployGate SDK for Android",
        )
    }

    override fun LibraryDefaultConfig.configureLibraryDefaultConfig() {
        FEATURE_FLAGS.forEach { (feature, flag) ->
            buildConfigField("int", feature, "$flag")
        }

        addManifestPlaceholders(META_DATA_ENTRIES)
    }
}