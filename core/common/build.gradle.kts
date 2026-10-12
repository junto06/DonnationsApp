plugins {
    alias(libs.plugins.donnations.android.library)
}

android {
    namespace = "com.donnations.core.common"
}

dependencies {
    // api: ViewModel, CoroutineScope and Job appear in this module's public extensions.
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.kotlinx.coroutines.core)

    testImplementation(testFixtures(projects.core.base))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
