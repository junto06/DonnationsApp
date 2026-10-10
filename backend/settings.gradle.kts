// Standalone build: kept out of the Android build so Spring and AGP plugins never share a classpath.
rootProject.name = "donnations-backend"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
