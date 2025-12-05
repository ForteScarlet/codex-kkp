import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.*
import javax.inject.Inject

/**
 * Gradle task to prepare standalone executables for release distribution.
 *
 * This task copies compiled native executables from the build output directory
 * to a release directory with uniform naming conventions. It's designed to prepare
 * standalone executable files that can be distributed independently of the plugin bundle.
 *
 * Key features:
 * - Copies executables from platform-specific build directories
 * - Applies uniform naming: codex-kkp-cli-{platform} (no file extension)
 * - Sets executable permissions (755 on Unix systems)
 * - Supports incremental builds and build cache
 *
 * This task follows Gradle best practices:
 * - Uses [FileSystemOperations] for file operations (injected service)
 * - Supports incremental builds via proper input/output annotations
 * - Supports build cache via [@CacheableTask] and [@PathSensitive]
 *
 * @see UnifiedSkillPackagingTask
 */
@CacheableTask
abstract class PrepareStandaloneExecutablesTask @Inject constructor(
    private val fsOps: FileSystemOperations
) : DefaultTask() {

    /**
     * Source directory containing platform-specific executable binaries.
     * This should point to the build/bin directory with subdirectories for each target platform.
     * Expected structure: executablesSourceDir/{targetName}/releaseExecutable/{executable-file}
     */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val executablesSourceDir: DirectoryProperty

    /**
     * Output directory for standalone executables.
     * Example: build/pluginsReleases/
     */
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    /**
     * Platform mapping: Kotlin Native target names to platform names.
     * Example: {"macosX64" -> "macosx64", "linuxX64" -> "linuxx64"}
     */
    @get:Input
    abstract val platformMapping: MapProperty<String, String>

    init {
        group = "distribution"
        description = "Prepares standalone executables for release distribution"
    }

    @TaskAction
    fun prepareExecutables() {
        val releasesDir = outputDir.get().asFile
        val binDir = executablesSourceDir.get().asFile
        val platforms = platformMapping.get()

        logger.lifecycle("Preparing standalone executables for ${platforms.size} platforms")

        releasesDir.mkdirs()

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
                val targetFile = releasesDir.resolve(targetFileName)

                fsOps.copy {
                    from(sourceFile)
                    into(releasesDir)
                    rename { targetFileName }

                    // Set Unix permissions: rwxr-xr-x (755)
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

        logger.lifecycle("Standalone executables prepared: ${releasesDir.absolutePath}")
        val summary = "  Executables: $copiedCount created" + if (skippedCount > 0) ", $skippedCount skipped" else ""
        logger.lifecycle(summary)
    }
}
