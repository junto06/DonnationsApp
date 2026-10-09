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
        }
    }
}
