@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Donnations"

// App root
include(":app")

// Core
include(":core:base")
include(":core:designsystem")
include(":core:network:base")
include(":core:network:impl")

// Domain
include(":domain:home")

// Data
include(":data:home")

// Feature (presentation)
include(":feature:home")
