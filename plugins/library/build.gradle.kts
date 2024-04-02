plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.22")
    implementation("com.android.tools.build:gradle:7.4.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
}

gradlePlugin {
    plugins {
        create("sdk") {
            id = "com.deploygate.plugins.sdk"
            implementationClass = "com.deploygate.plugins.SdkPlugin"
        }
        create("sdk-mock") {
            id = "com.deploygate.plugins.sdk-mock"
            implementationClass = "com.deploygate.plugins.SdkMockPlugin"
        }
    }
}
