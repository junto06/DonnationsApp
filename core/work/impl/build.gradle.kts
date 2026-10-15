plugins {
    alias(libs.plugins.donnations.android.library)
    alias(libs.plugins.donnations.hilt)
}

android {
    namespace = "com.donnations.core.work.impl"
}

dependencies {
    api(projects.core.work.base)
    implementation(projects.core.base)
}
