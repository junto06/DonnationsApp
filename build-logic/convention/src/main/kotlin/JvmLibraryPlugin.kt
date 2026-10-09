import com.donnations.buildlogic.ConventionPlugin
import com.donnations.buildlogic.configureKotlinJvm
import com.donnations.buildlogic.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class JvmLibraryPlugin : ConventionPlugin() {
    override fun Project.applyConvention() {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.jvm")
        }

        configureKotlinJvm()

        dependencies {
            "implementation"(libs.findLibrary("kotlinx-coroutines-core").get())
            "implementation"(libs.findLibrary("javax-inject").get())
            "testImplementation"(libs.findLibrary("junit").get())
            "testImplementation"(libs.findLibrary("kotlinx-coroutines-test").get())
            "testImplementation"(libs.findLibrary("turbine").get())
        }
    }
}
