package com.deploygate.plugins.ext

import com.deploygate.plugins.dsl.SdkExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

val Project.sdkExtension: SdkExtension
    get() = extensions.getByType()