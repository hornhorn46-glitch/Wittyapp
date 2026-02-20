plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.wittyapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.wittyapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            // Disable code shrinking and obfuscation for this example.
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Enable Jetpack Compose support.
    buildFeatures {
        compose = true
    }

    // Configure the Compose compiler version.
    composeOptions {
        kotlinCompilerExtensionVersion = "1.6.0"
    }

    // Exclude redundant license files from packaged APKs.
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Use the Compose BOM to manage versions of Compose libraries consistently.
    val composeBom = platform("androidx.compose:compose-bom:2026.01.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Material Design 3 library for Compose UI elements
    implementation("androidx.compose.material3:material3")
    // Integration with activities
    implementation("androidx.activity:activity-compose:1.11.0")
    // Integration with lifecycle view models
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    // Optional: additional Compose UI tooling and testing support
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}