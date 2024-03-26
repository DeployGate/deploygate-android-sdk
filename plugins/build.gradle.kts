plugins {
    `kotlin-dsl` apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
