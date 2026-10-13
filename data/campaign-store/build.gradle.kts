plugins {
    alias(libs.plugins.donnations.android.library)
    alias(libs.plugins.donnations.hilt)
    alias(libs.plugins.donnations.android.room)
}

android {
    namespace = "com.donnations.data.campaignstore"
    // FakeCampaignDao, shared with the data modules' tests via testFixtures(projects.data.campaignStore).
    testFixtures.enable = true
    // Robolectric runs the real Room DAO on the JVM.
    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    // Room's Flow return types appear in the DAO's public API.
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
