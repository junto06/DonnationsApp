import com.android.build.api.dsl.ApplicationExtension
import com.donnations.buildlogic.ConventionPlugin
import com.donnations.buildlogic.configureKotlinAndroid
import com.donnations.buildlogic.versionInt
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidAppPlugin : ConventionPlugin() {
    override fun Project.applyConvention() {
        with(pluginManager) {
            apply("com.android.application")
        }

        extensions.configure<ApplicationExtension> {
            configureKotlinAndroid(this)
            defaultConfig.targetSdk = versionInt("targetSdk")

            buildTypes {
                release {
                    isMinifyEnabled = false
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro"
                    )
                }
            }
        }
    }
}
