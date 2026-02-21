pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        // Android Gradle Plugin
        id("com.android.application") version "8.3.1"
        // Kotlin Android plugin
        id("org.jetbrains.kotlin.android") version "1.9.24"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "WittyApp"
include(":app")