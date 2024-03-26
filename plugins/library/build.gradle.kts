plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.22")
    implementation("com.android.tools.build:gradle:7.4.2")
}

gradlePlugin {
    plugins {
        create("sdk") {
            id = "com.deploygate.plugins.sdk"
            implementationClass = "com.deploygate.plugins.SdkPlugin"
        }
    }
}
