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
    implementation(projects.core.base)

    testImplementation(libs.junit)
    testImplementation(testFixtures(projects.core.base))
    testImplementation(libs.kotlinx.coroutines.test)
}
