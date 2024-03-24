package com.deploygate.plugins.dsl

import org.gradle.api.provider.Property

interface SdkExtension {

    val artifactId: Property<String>

    val displayName: Property<String>
    val description: Property<String>
}