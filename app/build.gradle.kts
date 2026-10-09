plugins {
    alias(libs.plugins.donnations.android.application)
    alias(libs.plugins.donnations.android.compose)
    alias(libs.plugins.donnations.hilt)
}

android {
    namespace = "com.donnations"

    defaultConfig {
        applicationId = "com.donnations"
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
}
