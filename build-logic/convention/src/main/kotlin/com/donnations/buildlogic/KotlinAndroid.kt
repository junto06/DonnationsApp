package com.donnations.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

private val Project.jdk: Int get() = versionInt("jdk")

internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension,
) {
    val jdkVersion = jdk
    commonExtension.apply {
        compileSdk = versionInt("compileSdk")

        defaultConfig.apply {
            minSdk = versionInt("minSdk")
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions.apply {
            sourceCompatibility = JavaVersion.toVersion(jdkVersion)
            targetCompatibility = JavaVersion.toVersion(jdkVersion)
            // minSdk 24 lacks java.time (API 26); desugaring backports it so domain models can use Instant.
            isCoreLibraryDesugaringEnabled = true
        }
    }

    dependencies {
        "coreLibraryDesugaring"(libs.findLibrary("desugar-jdk-libs").get())
    }

    extensions.configure<KotlinAndroidProjectExtension> {
        jvmToolchain(jdkVersion)
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(jdkVersion.toString()))
            freeCompilerArgs.addAll(
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
            )
        }
    }
}

internal fun Project.configureKotlinJvm() {
    val jdkVersion = jdk
    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.toVersion(jdkVersion)
        targetCompatibility = JavaVersion.toVersion(jdkVersion)
    }

    extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(jdkVersion)
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(jdkVersion.toString()))
            freeCompilerArgs.addAll(
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
            )
        }
    }
}
