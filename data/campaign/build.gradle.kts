plugins {
    alias(libs.plugins.donnations.android.library)
    alias(libs.plugins.donnations.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.donnations.data.campaign"
    // Robolectric runs WorkManager (scheduler and worker tests) on the JVM.
    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    implementation(projects.domain.campaign)
    implementation(projects.data.campaignStore)
    implementation(projects.core.network.base)
    implementation(projects.core.base)
    // SyncScheduler is implemented with WorkManager; the hilt compiler generates the @HiltWorker factories.
    implementation(projects.core.work.base)
    ksp(libs.androidx.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(testFixtures(projects.core.base))
    testImplementation(testFixtures(projects.data.campaignStore))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.work.testing)
}
