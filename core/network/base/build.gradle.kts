plugins {
    alias(libs.plugins.donnations.jvm.library)
}

// api: consumers get these transitively
dependencies {
    api(libs.retrofit)
    api(libs.retrofit.kotlinx.serialization)
    api(libs.okhttp)
    api(libs.kotlinx.serialization.json)
}
