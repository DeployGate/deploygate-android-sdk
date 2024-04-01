package com.deploygate.plugins.tasks

import com.deploygate.plugins.internal.SdkMetaData
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.plugins.ide.api.GeneratorTask
import org.gradle.plugins.ide.internal.generator.generator.Generator
import java.io.File

abstract class GenerateMetaDataJsonTask : GeneratorTask<SdkMetaData>() {
    @Input
    abstract fun getArtifactVersion(): Property<String>

    @Input
    abstract fun getFeatureFlags(): MapProperty<String, Int>

    @Input
    abstract fun getActiveFeatureFlags(): Property<Int>

    @Input
    abstract fun getMetaData(): MapProperty<String, String>

    init {
        generator = object : Generator<SdkMetaData> {
            override fun read(inputFile: File): SdkMetaData {
                return sdkMetaDataAdapter.fromJson(inputFile.readText())!!
            }

            override fun defaultInstance(): SdkMetaData {
                return SdkMetaData(
                    artifactVersion = "",
                    featureFlags = emptyMap(),
                    metaData = emptyMap(),
                    activeFeatureFlags = 0,
                )
            }

            override fun write(`object`: SdkMetaData, outputFile: File) {
                outputFile.writeText(
                    text = sdkMetaDataAdapter.toJson(`object`),
                    charset = Charsets.UTF_8,
                )
            }

            override fun configure(`object`: SdkMetaData) {
                `object`.apply {
                    this.artifactVersion = getArtifactVersion().get()
                    this.featureFlags = getFeatureFlags().get()
                    this.activeFeatureFlags = getActiveFeatureFlags().get()
                    this.metaData = getMetaData().get()
                }

                `object`.validate()
            }
        }
    }

    companion object {
        private val sdkMetaDataAdapter: JsonAdapter<SdkMetaData> = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
            .adapter(SdkMetaData::class.java)
            .failOnUnknown()
            .indent("  ")
    }
}