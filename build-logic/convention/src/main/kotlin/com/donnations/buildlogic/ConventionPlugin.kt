package com.donnations.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

// Base for convention plugins; subclasses implement applyConvention() with Project as receiver.
abstract class ConventionPlugin : Plugin<Project> {
    final override fun apply(target: Project) = target.applyConvention()

    protected abstract fun Project.applyConvention()
}
