plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

    namespace = "com.kadir.screentimeout"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kadir.screentimeout"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
0

