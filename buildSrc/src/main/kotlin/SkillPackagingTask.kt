import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import javax.inject.Inject

/**
 * Gradle task to prepare platform-specific skill bundles for Claude Code.
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
abstract class SkillPackagingTask @Inject constructor(
    private val fsOps: FileSystemOperations
) : DefaultTask() {

    /**
     * Platform name in lowercase (e.g., "macosx64", "mingwx64").
     * Used for naming the output directory.
     */
    @get:Input
    abstract val platformName: Property<String>

    /**
     * Kotlin/Native target name (e.g., "macosX64", "mingwX64").
     * Used for logging and identification.
     */
    @get:Input
    abstract val kotlinTarget: Property<String>

    /**
     * Source executable file (with .kexe or .exe extension).
     * Will be copied and renamed to 'codex-kkp-cli' without extension.
     *
     * Optional to support cross-platform builds where the host OS
     * cannot generate binaries for other platforms (e.g., Windows cannot build macOS binaries).
     */
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val executableFile: RegularFileProperty

    /**
     * Template directory containing skill documentation.
     * Expected structure: templateDir/codex-agent-collaboration/{SKILL.md, examples.md, ...}
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
     * Output directory for the prepared skill bundle.
     * This should be the specific skill directory (e.g., "build/skills/codex-agent-collaboration-macosx64"),
     * not a shared parent directory, to avoid Gradle task dependency conflicts.
     */
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        group = "distribution"
        description = "Prepares a platform-specific skill bundle for Claude Code"
    }

    @TaskAction
    fun prepareSkillBundle() {
        val platform = platformName.get()
        val target = kotlinTarget.get()
        val skillDir = outputDir.get().asFile
        val templateDirectory = templateDir.get().asFile

        logger.lifecycle("Preparing skill bundle for platform: $platform ($target)")

        // Sync documentation files from template using Gradle's sync operation
        // sync() ensures the destination matches the source exactly (removes stale files)
        fsOps.sync {
            from(templateDirectory.resolve("codex-agent-collaboration")) {
                include("SKILL.md", "examples.md", "outputs.md", "sandbox-modes.md")
            }
            into(skillDir)
        }

        logger.info("Synced documentation files to: ${skillDir.absolutePath}")

        // Copy executable with proper permissions using Gradle's copy operation
        // Skip if the executable file is not present (e.g., cross-platform build)
        if (executableFile.isPresent && executableFile.get().asFile.exists()) {
            fsOps.copy {
                from(executableFile)
                into(skillDir.resolve("executable"))
                rename { "codex-kkp-cli" }

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

            val execSize = executableFile.get().asFile.length() / 1024
            logger.lifecycle("Copied executable: ${executableFile.get().asFile.name} -> codex-kkp-cli ($execSize KB)")
        } else {
            logger.warn("Skipping executable copy for $platform: binary not available on this host OS")
        }

        logger.lifecycle("Skill bundle prepared: ${skillDir.absolutePath}")
    }
}
