package com.deploygate.plugins

import com.android.build.api.dsl.LibraryDefaultConfig
import com.deploygate.plugins.dsl.SdkExtension
import com.deploygate.plugins.internal.SdkExtensionImpl

class SdkMockPlugin : BaseSdkPlugin() {
    override fun createSdkExtension(): SdkExtension {
        return SdkExtensionImpl(
            displayName = "DeployGate SDK Mock",
            artifactId = "sdk-mock",
            description = "Mocked dummy DeployGate SDK for Android to reduce footprint of your release version app without code modification",
        )
    }

    override fun LibraryDefaultConfig.configureLibraryDefaultConfig() {
        // Do not add meta-data and so on for sdk-mock
    }
}