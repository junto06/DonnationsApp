import com.android.build.api.dsl.CommonExtension
import com.donnations.buildlogic.ConventionPlugin
import com.donnations.buildlogic.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class ComposePlugin : ConventionPlugin() {
    override fun Project.applyConvention() {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.plugin.compose")
        }

        configureAndroidCompose(extensions.getByName("android") as CommonExtension)
    }
}

private fun Project.configureAndroidCompose(
    commonExtension: CommonExtension,
) {
    commonExtension.buildFeatures.compose = true

    dependencies {
        val bom = libs.findLibrary("compose-bom").get()
        "implementation"(platform(bom))
        "androidTestImplementation"(platform(bom))
        "implementation"(libs.findLibrary("compose-ui").get())
        "implementation"(libs.findLibrary("compose-ui-graphics").get())
        "implementation"(libs.findLibrary("compose-ui-tooling-preview").get())
        "implementation"(libs.findLibrary("compose-material3").get())
        "debugImplementation"(libs.findLibrary("compose-ui-tooling").get())
    }
}