plugins {
    alias(libs.plugins.donnations.android.library)
    alias(libs.plugins.donnations.hilt)
    alias(libs.plugins.donnations.android.room)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.donnations.data.home"
}

dependencies {
    implementation(projects.domain.home)
    implementation(projects.data.campaignStore)
    implementation(projects.core.network.base)
    implementation(projects.core.base)

    testImplementation(libs.junit)
    testImplementation(testFixtures(projects.core.base))
    testImplementation(testFixtures(projects.data.campaignStore))
    testImplementation(libs.kotlinx.coroutines.test)
}
