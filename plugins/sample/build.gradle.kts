plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.22")
    implementation("com.android.tools.build:gradle:7.4.2")
    implementation("com.project.starter:easylauncher:5.0.1")
}

gradlePlugin {
    plugins {
        create("sample") {
            id = "com.deploygate.plugins.sample-app"
            implementationClass = "com.deploygate.plugins.SampleAppPlugin"
        }
    }
}
