package com.donnations.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class ConventionPlugin : Plugin<Project> {
    final override fun apply(target: Project) = target.applyConvention()

    protected abstract fun Project.applyConvention()
}
