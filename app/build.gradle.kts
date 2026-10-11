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
        // PROD unless -Pstaging
        val environment = if (providers.gradleProperty("staging").isPresent) "STAGING" else "PROD"
        buildConfigField("String", "ENVIRONMENT", "\"$environment\"")
    }

    buildFeatures.buildConfig = true
}

dependencies {
    implementation(projects.core.base)
    implementation(projects.core.designsystem)
    implementation(projects.feature.home)
    implementation(projects.feature.campaign)
    // data is only here so Hilt can assemble its bindings
    implementation(projects.data.home)
    implementation(projects.data.campaign)

    // Only the app sees network:impl; features compile against network:base
    implementation(projects.core.network.impl)
    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
}
