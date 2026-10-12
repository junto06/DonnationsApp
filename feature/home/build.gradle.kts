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
    implementation(projects.core.common)

    testImplementation(testFixtures(projects.core.base))
}
