plugins {
    alias(libs.plugins.donnations.android.library)
    alias(libs.plugins.donnations.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.donnations.data.donation"
}

dependencies {
    implementation(projects.domain.donation)
    implementation(projects.core.network.base)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
