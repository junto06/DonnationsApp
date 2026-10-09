import com.android.build.api.dsl.LibraryExtension
import com.donnations.buildlogic.ConventionPlugin
import com.donnations.buildlogic.configureKotlinAndroid
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryPlugin : ConventionPlugin() {
    override fun Project.applyConvention() {
        with(pluginManager) {
            apply("com.android.library")
        }

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this)
            // Off by default for faster builds
            // opt in per module with androidResources.enable = true
            androidResources.enable = false
            val consumerRules = file("consumer-rules.pro")
            if (consumerRules.exists()) {
                defaultConfig.consumerProguardFiles(consumerRules)
            }
        }
    }
}
