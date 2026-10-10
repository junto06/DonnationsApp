plugins {
    alias(libs.plugins.donnations.android.library)
    alias(libs.plugins.donnations.hilt)
}

android {
    namespace = "com.donnations.core.network.impl"
}

dependencies {
    api(projects.core.network.base)
    implementation(projects.core.base)
    implementation(libs.okhttp.logging.interceptor)
}
