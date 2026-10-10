import com.donnations.buildlogic.ConventionPlugin
import com.donnations.buildlogic.libs
import com.donnations.buildlogic.pluginId
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class FeaturePlugin : ConventionPlugin() {
    override fun Project.applyConvention() {
        with(pluginManager) {
            apply(libs.pluginId("donnations-android-library"))
            apply(libs.pluginId("donnations-android-compose"))
            apply(libs.pluginId("donnations-hilt"))
        }

        dependencies {
            "implementation"(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
            "implementation"(libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
            "implementation"(libs.findLibrary("androidx-navigation-compose").get())
            "implementation"(libs.findLibrary("androidx-hilt-lifecycle-viewmodel-compose").get())
            "implementation"(libs.findLibrary("kotlinx-coroutines-android").get())
            
            "testImplementation"(libs.findLibrary("junit").get())
            "testImplementation"(libs.findLibrary("kotlinx-coroutines-test").get())
            "testImplementation"(libs.findLibrary("turbine").get())
        }
    }
}
