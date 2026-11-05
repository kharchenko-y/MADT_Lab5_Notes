plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.madtlab5"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.madtlab5"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions { unitTests.isReturnDefaultValues = true }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
