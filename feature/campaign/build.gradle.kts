plugins {
    alias(libs.plugins.donnations.android.feature)
}

android {
    namespace = "com.donnations.feature.campaign"
    androidResources.enable = true
}

dependencies {
    implementation(projects.domain.campaign)
    implementation(projects.core.base)
    implementation(projects.core.designsystem)
}
