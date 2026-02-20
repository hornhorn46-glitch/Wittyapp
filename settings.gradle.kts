pluginManagement {
    repositories {
        google()
        mavenCentral()
    }
    plugins {
        // Android application plugin
        id("com.android.application") version "8.3.1"
        // Kotlin Android plugin
        id("org.jetbrains.kotlin.android") version "1.9.21"
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