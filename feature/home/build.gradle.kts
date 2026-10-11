plugins {
    alias(libs.plugins.donnations.android.feature)
}

android {
    namespace = "com.donnations.feature.home"
    androidResources.enable = true
}

dependencies {
    implementation(projects.domain.home)
    implementation(projects.core.designsystem)
    implementation(projects.core.base)
}
