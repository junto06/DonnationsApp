import com.donnations.buildlogic.ConventionPlugin
import com.donnations.buildlogic.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class HiltAndroidPlugin : ConventionPlugin() {
    override fun Project.applyConvention() {
        with(pluginManager) {
            apply("com.google.devtools.ksp")
            apply("com.google.dagger.hilt.android")
        }

        dependencies {
            "implementation"(libs.findLibrary("hilt-android").get())
            "ksp"(libs.findLibrary("hilt-compiler").get())
            // hilt-android pulls annotation-experimental 1.4.x, whose lint check falsely flags @Serializable
            // with InternalSerializationApi in the IDE (KTIJ-34986); 1.5.1+ fixes it.
            "implementation"(libs.findLibrary("androidx-annotation-experimental").get())
        }
    }
}
