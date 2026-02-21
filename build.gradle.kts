// Root build file. Module-level config is in app/build.gradle.kts

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}