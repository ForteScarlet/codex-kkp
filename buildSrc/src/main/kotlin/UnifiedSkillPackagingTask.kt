import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import javax.inject.Inject

/**
 * Gradle task to prepare a unified multi-platform skill bundle for Claude Code.
 *
 * This task creates a single skill directory containing executables for all supported platforms,
 * rather than creating separate platform-specific skill directories. Claude Code will automatically
 * select the appropriate executable based on the runtime platform.
 *
 * This task follows Gradle best practices:
 * - Uses [FileSystemOperations] for file operations (injected service)
 * - Supports incremental builds via proper input/output annotations
 * - Supports build cache via [@CacheableTask] and [@PathSensitive]
 * - Uses Gradle's [filePermissions] API for setting executable permissions
 *
 * ZIP creation is handled separately by a standard [org.gradle.api.tasks.bundling.Zip] task
 * for better separation of concerns and native Gradle support.
 *
 * @see org.gradle.api.tasks.bundling.Zip
 */
@CacheableTask
abstract class UnifiedSkillPackagingTask @Inject constructor(
    private val fsOps: FileSystemOperations
) : DefaultTask() {

    /**
     * Platform executables directory.
     * This should point to the build/bin directory containing platform subdirectories.
     */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val executablesSourceDir: DirectoryProperty

    /**
     * Template directory containing skill documentation.
     * Expected structure: templateDir/{SKILL.md, examples.md, outputs.md, sandbox-modes.md}
     */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val templateDir: DirectoryProperty

    /**
     * Version string for this release.
     * Currently stored for potential future use (e.g., version file generation).
     */
    @get:Input
    abstract val version: Property<String>

    /**
     * Output directory for the unified skill bundle.
     * This will contain all documentation and the executables/ subdirectory.
     * Example: build/skills/codex-agent-collaboration/
     */
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    /**
     * Platform mapping: Kotlin Native target names to platform names.
     * Used to find and rename executables.
     */
    @get:Input
    abstract val platformMapping: MapProperty<String, String>

    init {
        group = "distribution"
        description = "Prepares unified multi-platform skill bundle for Claude Code"
    }

    @TaskAction
    fun packageUnifiedSkill() {
        val skillDir = outputDir.get().asFile
        val templateDirectory = templateDir.get().asFile
        val binDir = executablesSourceDir.get().asFile
        val platforms = platformMapping.get()

        logger.lifecycle("Preparing unified skill bundle for ${platforms.size} platforms")

        // Sync documentation files from template using Gradle's sync operation
        // sync() ensures the destination matches the source exactly (removes stale files)
        fsOps.sync {
            from(templateDirectory) {
                include("SKILL.md", "examples.md", "outputs.md", "sandbox-modes.md")
            }
            into(skillDir)
        }

        logger.info("Synced documentation files to: ${skillDir.absolutePath}")

        // Create executables directory for all platform binaries
        val executablesDir = skillDir.resolve("executables")
        executablesDir.mkdirs()

        // Copy all platform executables with uniform naming
        var copiedCount = 0
        var skippedCount = 0

        platforms.forEach { (targetName, platformName) ->
            // Find executable in bin/{targetName}/releaseExecutable/
            val platformDir = binDir.resolve("$targetName/releaseExecutable")
            val possibleExecs = listOf(
                platformDir.resolve("codex-kkp-cli.kexe"),
                platformDir.resolve("codex-kkp-cli.exe")
            )

            val sourceFile = possibleExecs.firstOrNull { it.exists() }

            if (sourceFile != null && sourceFile.exists()) {
                // Uniform naming: codex-kkp-cli-{platform} (no extension)
                val targetFileName = "codex-kkp-cli-$platformName"
                val targetFile = executablesDir.resolve(targetFileName)

                fsOps.copy {
                    from(sourceFile)
                    into(executablesDir)
                    rename { targetFileName }

                    // Set Unix permissions: rwxr-xr-x (755)
                    // This is the Gradle 8.3+ way to handle file permissions
                    filePermissions {
                        user {
                            read = true
                            write = true
                            execute = true
                        }
                        group {
                            read = true
                            execute = true
                        }
                        other {
                            read = true
                            execute = true
                        }
                    }
                }

                val execSize = sourceFile.length() / 1024
                logger.lifecycle("  ✓ $targetFileName ($execSize KB)")
                copiedCount++
            } else {
                logger.lifecycle("  ⊘ codex-kkp-cli-$platformName - not available on this host OS")
                skippedCount++
            }
        }

        logger.lifecycle("Unified skill bundle prepared: ${skillDir.absolutePath}")
        logger.lifecycle("  Platforms included: $copiedCount" + if (skippedCount > 0) ", $skippedCount skipped" else "")
    }
}
