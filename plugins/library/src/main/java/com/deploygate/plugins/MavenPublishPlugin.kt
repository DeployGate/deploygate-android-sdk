package com.deploygate.plugins

import com.deploygate.plugins.ext.internalApiLibraryExtension
import com.deploygate.plugins.ext.publishingExtension
import com.deploygate.plugins.ext.sdkExtension
import com.deploygate.plugins.ext.signingExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.plugins.signing.SigningExtension
import java.util.Locale

class MavenPublishPlugin : Plugin<Project> {
    companion object {
        private val RELEASE_VERSION_REGEX = "^\\d+\\.\\d+\\.\\d+\$".toRegex()
    }

    override fun apply(target: Project) {
        target.apply(plugin = "maven-publish")
        target.apply(plugin = "signing")

        val artifactVersion = target.version as String

        val repositoryUsername = target.findProperty("NEXUS_USERNAME") as? String
        val repositoryPassword = target.findProperty("NEXUS_PASSWORD") as? String
        val signingKey = target.findProperty("signingKey") as? String
        val signingPassword = target.findProperty("signingPassword") as? String

        val isRelease = artifactVersion.matches(RELEASE_VERSION_REGEX)

        target.internalApiLibraryExtension.libraryVariants.configureEach {
            val variant = this

            target.tasks.register<Exec>("verifyBytecodeVersion${variant.name.capitalize(Locale.ROOT)}")
                .configure {
                    dependsOn(variant.assembleProvider)

                    commandLine(
                        target.rootProject.file("scripts/verify-bytecode-version"),
                        "--aar",
                        variant.packageLibraryProvider.flatMap { it.archiveFile }.get(),
                        "--java",
                        BaseSdkPlugin.JAVA_VERSION.toString()
                    )
                }
        }

        target.publishingExtension.configurePublishingExtension(
            target = target,
            artifactVersion = artifactVersion,
            isRelease = isRelease,
            repositoryUsername = repositoryUsername,
            repositoryPassword = repositoryPassword,
        )
        target.signingExtension.configureSigningExtension(
            isSigningRequired = isRelease,
            signingKey = signingKey,
            signingPassword = signingPassword,
            publications = target.extensions.getByType<PublishingExtension>().publications,
        )

        target.gradle.taskGraph.whenReady {
            with(target.signingExtension) {
                isRequired = isRequired && hasTask("publishReleasePublicationToMavenRepository")
            }
        }
    }

    private fun PublishingExtension.configurePublishingExtension(
        target: Project,
        artifactVersion: String,
        isRelease: Boolean,
        repositoryUsername: String?,
        repositoryPassword: String?,
    ) {
        repositories {
            maven {
                setUrl(
                    if (isRelease) {
                        "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                    } else {
                        "https://oss.sonatype.org/content/repositories/snapshots/"
                    }
                )

                credentials(PasswordCredentials::class.java) {
                    username = repositoryUsername
                    password = repositoryPassword
                }
            }
        }


        publications {
            create<MavenPublication>("release") {
                // This block would be evaluated before components.getByName("release") was created

                groupId = "com.deploygate"
                version = artifactVersion

                pom {
                    name.set(target.sdkExtension.displayName)
                    description.set(target.sdkExtension.description)
                    url.set("https://github.com/DeployGate/deploygate-android-sdk")

                    licenses {
                        license {
                            name.set("The Apache Software License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            distribution.set("repo")
                        }
                    }

                    developers {
                        developer {
                            id.set("deploygate")
                            name.set("DeployGate")
                        }
                    }

                    scm {
                        url.set("https://github.com/DeployGate/deploygate-android-sdk")
                    }
                }
            }
        }

        target.afterEvaluate {
            publications {
                // Configure some values after AGP has been configured
                named<MavenPublication>("release") {
                    from(components.getByName("release"))
                    artifactId = sdkExtension.artifactId
                }
            }
        }
    }


    private fun SigningExtension.configureSigningExtension(
        isSigningRequired: Boolean,
        signingKey: String?,
        signingPassword: String?,
        publications: PublicationContainer,
    ) {
        isRequired = isSigningRequired
        useInMemoryPgpKeys(signingKey, signingPassword)

        publications.configureEach {
            sign(this)
        }
    }
}