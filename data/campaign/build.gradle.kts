plugins {
    alias(libs.plugins.donnations.android.library)
    alias(libs.plugins.donnations.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.donnations.data.campaign"
}

dependencies {
    implementation(projects.domain.campaign)
    implementation(projects.core.network.base)
}
