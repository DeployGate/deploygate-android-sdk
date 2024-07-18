package com.deploygate.plugins

import com.android.build.api.dsl.LibraryDefaultConfig
import com.android.build.api.dsl.LibraryExtension
import com.deploygate.plugins.dsl.SdkExtension
import com.deploygate.plugins.ext.libraryExtension
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

abstract class BaseSdkPlugin : Plugin<Project> {
    companion object {
        /**
         * sdk/java/com/deploygate/sdk/HostAppTest.java needs to be changed for a new release
         */
        const val ARTIFACT_VERSION = "4.8.0-alpha01"

        val JAVA_VERSION = JavaVersion.VERSION_1_7
    }

    protected val artifactVersion: String
        get() = ARTIFACT_VERSION

    override fun apply(target: Project) {
        target.version = ARTIFACT_VERSION

        target.extensions.add("sdk", createSdkExtension())

        target.apply(plugin = "com.android.library")
        target.apply<MavenPublishPlugin>()

        target.libraryExtension.configureLibraryExtension()

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

    private fun LibraryExtension.configureLibraryExtension() {
        compileSdk = 33

        defaultConfig {
            minSdk = 14
            // TODO remove this property and set the default sdk version for robolectric test instead
            @Suppress("DEPRECATION")
            targetSdk = 33

            configureLibraryDefaultConfig()
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

                all {
                    it.jvmArgs(
                        "-Xmx1g",
                    )
                }
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

    abstract fun createSdkExtension(): SdkExtension
    abstract fun LibraryDefaultConfig.configureLibraryDefaultConfig()
}