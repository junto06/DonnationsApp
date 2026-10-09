plugins {
    `kotlin-dsl`
}

group = "com.donnations.buildlogic"

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.jdk.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.jdk.get())
}

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.donnations.android.application.get().pluginId
            implementationClass = "AndroidAppPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.donnations.android.library.get().pluginId
            implementationClass = "AndroidLibraryPlugin"
        }
        register("androidCompose") {
            id = libs.plugins.donnations.android.compose.get().pluginId
            implementationClass = "ComposePlugin"
        }
        register("androidFeature") {
            id = libs.plugins.donnations.android.feature.get().pluginId
            implementationClass = "FeaturePlugin"
        }
        register("jvmLibrary") {
            id = libs.plugins.donnations.jvm.library.get().pluginId
            implementationClass = "JvmLibraryPlugin"
        }
        register("hilt") {
            id = libs.plugins.donnations.hilt.get().pluginId
            implementationClass = "HiltAndroidPlugin"
        }
    }
}
