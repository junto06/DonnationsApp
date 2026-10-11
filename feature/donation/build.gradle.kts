plugins {
    alias(libs.plugins.donnations.android.feature)
}

android {
    namespace = "com.donnations.feature.donation"
    androidResources.enable = true
}

dependencies {
    implementation(projects.domain.donation)
    implementation(projects.core.base)
    implementation(projects.core.designsystem)
}
