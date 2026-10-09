package com.donnations.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun Project.versionInt(alias: String): Int =
    libs.findVersion(alias).get().requiredVersion.toInt()

fun VersionCatalog.pluginId(alias: String): String = findPlugin(alias).get().get().pluginId
