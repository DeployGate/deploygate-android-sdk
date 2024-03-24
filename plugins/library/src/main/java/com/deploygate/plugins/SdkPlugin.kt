package com.deploygate.plugins

import com.android.build.api.dsl.LibraryExtension
import com.deploygate.plugins.dsl.SdkExtension
import com.deploygate.plugins.ext.libraryExtension
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies

class SdkPlugin : Plugin<Project> {
    companion object {
        /**
         * sdk/java/com/deploygate/sdk/HostAppTest.java needs to be changed for a new release
         */
        private const val ARTIFACT_VERSION = "4.7.1"

        val JAVA_VERSION = JavaVersion.VERSION_1_8
    }

    override fun apply(target: Project) {
        target.version = ARTIFACT_VERSION

        target.extensions.create<SdkExtension>("sdk")

        target.apply(plugin = "com.android.library")
        target.apply<MavenPublishPlugin>()

        target.libraryExtension.configureLibraryExtension(
            artifactVersion = target.version as String,
        )

        target.tasks.register("verifyReleaseVersion") {
            doLast {
                if (target.version != System.getenv("RELEASE_VERSION")) {
                    throw GradleException("${target.version} does not equal to ${System.getenv("RELEASE_VERSION")}")
                }
            }
        }

        target.dependencies {
            add("testImplementation", "androidx.test:runner:1.5.2")
            add("testImplementation", "androidx.test.ext:junit:1.1.5")
            add("testImplementation", "org.robolectric:robolectric:4.10.3")
            add("testImplementation", "androidx.test:rules:1.5.0")
            add("testImplementation", "com.google.truth:truth:1.0")
        }

    }

    fun LibraryExtension.configureLibraryExtension(
        artifactVersion: String,
    ) {
        compileSdk = 33

        defaultConfig {
            minSdk = 14

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

        buildTypes {
            release {
                isMinifyEnabled = false
            }
        }

        compileOptions {
            sourceCompatibility(JAVA_VERSION)
            targetCompatibility(JAVA_VERSION)
        }

        testOptions {
            unitTests {
                isIncludeAndroidResources = true
            }
            unitTests.all {
                it.jvmArgs(
                    "-Xmx1g",
                )
            }
        }

        buildFeatures {
            buildConfig = true
        }

        lint {
            abortOnError = false
        }

        publishing {
            singleVariant("release") {
                withJavadocJar()
                withSourcesJar()
            }
        }
    }
}