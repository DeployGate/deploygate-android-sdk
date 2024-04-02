package com.deploygate.plugins.internal

import com.deploygate.plugins.dsl.SdkExtension

data class SdkExtensionImpl(
    override val artifactId: String,
    override val displayName: String,
    override val description: String,
) : SdkExtension