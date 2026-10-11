plugins {
    alias(libs.plugins.donnations.android.library)
    alias(libs.plugins.donnations.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.donnations.data.home"
}

dependencies {
    implementation(projects.domain.home)
    implementation(projects.core.network.base)

    testImplementation(libs.junit)
}
