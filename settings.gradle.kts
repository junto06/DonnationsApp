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
include(":core:common")
include(":core:model")
include(":core:designsystem")
include(":core:network:base")
include(":core:network:impl")
include(":core:work:base")
include(":core:work:impl")

// Domain
include(":domain:home")
include(":domain:campaign")
include(":domain:donation")

// Data
include(":data:campaign-store")
include(":data:home")
include(":data:campaign")
include(":data:donation")

// Feature (presentation)
include(":feature:home")
include(":feature:campaign")
include(":feature:donation")
