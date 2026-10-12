import androidx.room.gradle.RoomExtension
import com.donnations.buildlogic.ConventionPlugin
import com.donnations.buildlogic.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class RoomPlugin : ConventionPlugin() {
    override fun Project.applyConvention() {
        with(pluginManager) {
            apply("androidx.room")
            apply("com.google.devtools.ksp")
        }

        // Exported schemas are committed, so schema changes show up in review.
        extensions.configure<RoomExtension> {
            schemaDirectory("$projectDir/schemas")
        }

        dependencies {
            "implementation"(libs.findLibrary("room-runtime").get())
            "ksp"(libs.findLibrary("room-compiler").get())
        }
    }
}
