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
include(":core:model")
include(":core:designsystem")
include(":core:network:base")
include(":core:network:impl")

// Domain
include(":domain:home")
include(":domain:campaign")

// Data
include(":data:home")
include(":data:campaign")

// Feature (presentation)
include(":feature:home")
include(":feature:campaign")
