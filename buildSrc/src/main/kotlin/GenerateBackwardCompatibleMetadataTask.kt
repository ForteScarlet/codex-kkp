import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

/**
 * Gradle task to generate backward-compatible marketplace.json for plugin-only structure.
 *
 * This task generates the legacy marketplace.json format that is placed alongside
 * the plugin directory for backward compatibility with older Claude Code versions or
 * installations that expect the plugin-only structure.
 *
 * The generated marketplace.json is placed at:
 * - build/plugins/.claude-plugin/marketplace.json
 *
 * This is different from the new marketplace structure where marketplace.json is at the
 * marketplace root and references plugins as subdirectories.
 *
 * Key features:
 * - Generates marketplace.json using the [marketPlaceJson] function
 * - Supports incremental builds (only regenerates when version changes)
 * - Supports build cache via [@CacheableTask]
 * - Minimal and focused task implementation
 *
 * This task follows Gradle best practices:
 * - Uses [Property] for inputs with [@Input] annotation
 * - Uses [RegularFileProperty] for output with [@OutputFile] annotation
 * - Supports incremental builds and build cache
 *
 * @see marketPlaceJson
 */
@CacheableTask
abstract class GenerateBackwardCompatibleMetadataTask : DefaultTask() {

    /**
     * Version string for the marketplace and plugin.
     * Changes to this value will trigger task re-execution.
     */
    @get:Input
    abstract val version: Property<String>

    /**
     * Output marketplace.json file.
     * Example: build/plugins/.claude-plugin/marketplace.json
     */
    @get:OutputFile
    abstract val marketplaceJsonFile: RegularFileProperty

    init {
        group = "distribution"
        description = "Generates backward-compatible marketplace.json for plugin-only structure"
    }

    @TaskAction
    fun generate() {
        val outputFile = marketplaceJsonFile.get().asFile
        val versionStr = version.get()

        // Ensure parent directory exists
        outputFile.parentFile.mkdirs()

        // Generate and write marketplace.json content
        val content = marketPlaceJson(versionStr)
        outputFile.writeText(content)

        logger.lifecycle("Generated backward-compatible marketplace.json")
        logger.lifecycle("  Location: ${outputFile.absolutePath}")
        logger.lifecycle("  Version: $versionStr")
    }
}
