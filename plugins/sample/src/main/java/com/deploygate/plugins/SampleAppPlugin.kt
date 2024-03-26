package com.deploygate.plugins

import com.android.build.gradle.AppExtension
import com.project.starter.easylauncher.filter.ColorRibbonFilter
import com.project.starter.easylauncher.plugin.EasyLauncherExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.invoke

class SampleAppPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.apply(plugin = "com.android.application")
        target.apply(plugin = "com.starter.easylauncher")

        target.extensions.findByType(AppExtension::class.java)?.configureAppExtension()
        target.extensions.findByType(EasyLauncherExtension::class.java)
            ?.configureEasyLauncherExtension()
    }

    private fun AppExtension.configureAppExtension() {
        compileSdkVersion(33)

        defaultConfig {
            applicationId = "com.deploygate.sample"
            minSdk = 14
            targetSdk = 33
        }

        buildTypes {
            named("debug") {
                applicationIdSuffix = ".debug"
            }
            named("release") {
                minifyEnabled(true)
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-project.txt",
                )
            }
            create("distribute") {
                debuggable(true)
                matchingFallbacks += "release"
            }
        }

        flavorDimensions("mode")

        productFlavors {
            create("devreal") {
                dimension("mode")
                applicationIdSuffix(".realsdk.dev")
            }
            create("devmock") {
                dimension("mode")
                applicationIdSuffix(".mocksdk.dev")
            }
            create("stablereal") {
                dimension("mode")
                applicationIdSuffix(".realsdk.stable")
            }
            create("stablemock") {
                dimension("mode")
                applicationIdSuffix(".mocksdk.stable")
            }
        }

        compileOptions {
            sourceCompatibility(JavaVersion.VERSION_1_8)
            targetCompatibility(JavaVersion.VERSION_1_8)
        }

        lintOptions {
            isAbortOnError = false
        }
    }

    private fun EasyLauncherExtension.configureEasyLauncherExtension() {
        defaultFlavorNaming(true)

        productFlavors {
            create("devreal") {
                filters(
                    customRibbon(
                        label = "devreal",
                        ribbonColor = "#55E74C3C",
                        labelColor = "#FFFFFF",
                        gravity = ColorRibbonFilter.Gravity.TOPRIGHT,
                    ),
                )
            }
            create("devmock") {
                filters(
                    customRibbon(
                        label = "devmock",
                        ribbonColor = "#556600CC",
                        labelColor = "#FFFFFF",
                        gravity = ColorRibbonFilter.Gravity.TOPRIGHT,
                    ),
                )
            }
            create("stablereal") {
                enable(false)
            }
            create("stablemock") {
                enable(false)
            }
        }

        buildTypes {
            create("debug") {
                filters(
                    customRibbon(
                        label = "debug",
                        ribbonColor = "#5574924",
                        labelColor = "#FFFFFF",
                        gravity = ColorRibbonFilter.Gravity.BOTTOM,
                    ),
                )
            }
            create("distribute") {
                enable(false)
            }
            create("release") {
                enable(false)
            }
        }
    }
}