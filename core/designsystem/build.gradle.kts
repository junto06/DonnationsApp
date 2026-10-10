plugins {
    alias(libs.plugins.donnations.android.library)
    alias(libs.plugins.donnations.android.compose)
}

android {
    namespace = "com.donnations.core.designsystem"
}

dependencies {
    implementation(libs.coil.compose)
}
