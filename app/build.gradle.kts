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
    implementation(projects.feature.donation)
    // data is only here so Hilt can assemble its bindings
    implementation(projects.data.home)
    implementation(projects.data.campaign)
    implementation(projects.data.donation)

    // Only the app sees network:impl; features compile against network:base
    implementation(projects.core.network.impl)
    // Only the app sees work:impl (WorkManager setup); data modules compile against work:base
    implementation(projects.core.work.impl)
    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
}
