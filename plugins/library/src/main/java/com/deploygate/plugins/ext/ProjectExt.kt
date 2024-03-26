package com.deploygate.plugins.ext

import com.android.build.api.dsl.LibraryExtension
import com.deploygate.plugins.dsl.SdkExtension
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugins.signing.SigningExtension

val Project.sdkExtension: SdkExtension
    get() = extensions.getByType()

val Project.libraryExtension: LibraryExtension
    get() = extensions.getByType()

val Project.signingExtension: SigningExtension
    get() = extensions.getByType()

val Project.publishingExtension: PublishingExtension
    get() = extensions.getByType()

val Project.internalApiLibraryExtension: com.android.build.gradle.LibraryExtension
    get() = extensions.getByType()