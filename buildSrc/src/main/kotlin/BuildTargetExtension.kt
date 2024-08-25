import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

open class BuildTargetExtension {
    lateinit var androidCompileSdk: Provider<Int>
    lateinit var androidMinSdk: Provider<Int>
    lateinit var androidTargetSdk: Provider<Int>
    lateinit var javaVersion: Provider<JavaVersion>
    lateinit var composeCompilerDestination: Provider<Directory>
    lateinit var composeCompilerMetrics: Provider<Boolean>
    lateinit var composeCompilerReports: Provider<Boolean>
}

fun Project.buildTarget(block: BuildTargetExtension.() -> Unit) {
    extensions.create("buildTargetExtension", BuildTargetExtension::class.java).apply(block)
}
