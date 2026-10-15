plugins {
    alias(libs.plugins.donnations.android.library)
}

android {
    namespace = "com.donnations.core.work.base"
}

dependencies {
    // No code: data modules implement their domain's sync contracts with these (like core:network:base).
    // Modules declaring @HiltWorker classes also add ksp(libs.androidx.hilt.compiler).
    api(libs.androidx.work.runtime.ktx)
    api(libs.androidx.hilt.work)
}
