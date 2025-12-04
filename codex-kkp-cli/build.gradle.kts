import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    applyDefaultHierarchyTemplate()

    listOf(
        macosX64(),
        macosArm64(),
        mingwX64(),
        linuxX64(),
        linuxArm64(),
    ).forEach {
        it.binaries.executable {
            // binaryOption("smallBinary", "true")
            binaryOption("latin1Strings", "true")
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":codex-kkp-sdk"))
            api(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

// Example: How to iterate over all native targets and access their release binaries
// kotlin.targets.withType<KotlinNativeTarget> {
//     val releaseBinary = binaries.findExecutable(RELEASE)
//     println("Native target: $name, binary: ${releaseBinary?.outputFile}")
// }

// ============================================================
// Release and Packaging Tasks
// ============================================================

/**
 * Task to build release executables for all platforms.
 * Dynamically discovers all KotlinNativeTarget release binaries.
 */
tasks.register("linkReleaseExecutableMultiplatform") {
    group = "build"
    description = "Builds release executables for all platforms"

    // Dynamically depend on all native target release link tasks
    val linkTasks = kotlin.targets.withType<KotlinNativeTarget>().mapNotNull { target ->
        target.binaries.findExecutable(NativeBuildType.RELEASE)?.linkTaskProvider
    }
    dependsOn(linkTasks)
}

/**
 * Register packaging tasks for each platform dynamically.
 *
 * For each Kotlin Native target, this creates two tasks:
 * 1. `prepareSkill{Platform}` - Prepares the skill directory (copies docs + executable)
 * 2. `packageSkill{Platform}` - Creates the ZIP archive using Gradle's native Zip task
 *
 * This separation follows Gradle best practices:
 * - Single responsibility per task
 * - Uses Gradle's built-in Zip task for archive creation
 * - Better incremental build and caching support
 */
kotlin.targets.withType<KotlinNativeTarget> {
    val releaseBinary = binaries.findExecutable(NativeBuildType.RELEASE)

    if (releaseBinary != null) {
        val targetName = name // e.g., "macosX64"
        val platformName = targetName.lowercase() // e.g., "macosx64"
        val capitalizedTarget = targetName.replaceFirstChar { it.uppercase() }
        val skillDirName = "codex-agent-collaboration-$platformName"
        val skillsOutputDir = layout.buildDirectory.dir("skills")
        val skillsReleasesOutputDir = layout.buildDirectory.dir("skillsReleases")

        // The specific output directory for this platform's skill bundle
        val skillOutputDir = skillsOutputDir.map { it.dir(skillDirName) }

        // Task 1: Prepare skill directory (copy docs + executable)
        val prepareTask = tasks.register<SkillPackagingTask>("prepareSkill$capitalizedTarget") {
            group = "distribution"
            description = "Prepares skill bundle directory for $targetName platform"

            this.platformName.set(platformName)
            this.kotlinTarget.set(targetName)
            this.executableFile.set(releaseBinary.outputFile)
            this.templateDir.set(rootProject.file("claude-code-skills-template"))
            this.version.set(project.version.toString())
            this.outputDir.set(skillOutputDir)

            dependsOn(releaseBinary.linkTaskProvider)

            // Skip task if binary doesn't exist (e.g., cross-platform build on incompatible host)
            onlyIf {
                val binaryFile = releaseBinary.outputFile
                val exists = binaryFile.exists()
                if (!exists) {
                    logger.lifecycle("Skipping $name: binary not available for $targetName on this host OS")
                }
                exists
            }
        }

        // Task 2: Create ZIP archive using Gradle's native Zip task
        tasks.register<Zip>("packageSkill$capitalizedTarget") {
            group = "distribution"
            description = "Packages skill bundle ZIP for $targetName platform"

            dependsOn(prepareTask)

            // Configure ZIP contents - include the skill directory with proper root name
            from(prepareTask.flatMap { it.outputDir }) {
                into(skillDirName) // Ensure ZIP has the skill directory as root
            }

            // Configure ZIP file output
            archiveBaseName.set(skillDirName)
            archiveVersion.set("") // No version suffix in filename
            archiveExtension.set("zip")
            destinationDirectory.set(skillsReleasesOutputDir)

            // ZIP best practices
            isPreserveFileTimestamps = false  // Reproducible builds
            isReproducibleFileOrder = true    // Consistent file order

            // Skip task if binary doesn't exist (same condition as prepare task)
            onlyIf {
                val binaryFile = releaseBinary.outputFile
                val exists = binaryFile.exists()
                if (!exists) {
                    logger.lifecycle("Skipping $name: binary not available for $targetName on this host OS")
                }
                exists
            }

            doLast {
                logger.lifecycle("Created: ${archiveFile.get().asFile.name} (${archiveFile.get().asFile.length() / 1024} KB)")
            }
        }
    }
}

/**
 * Meta task to prepare all platform skill directories (without ZIP).
 */
tasks.register("prepareAllSkills") {
    group = "distribution"
    description = "Prepares skill bundle directories for all platforms"

    dependsOn(tasks.matching { it.name.startsWith("prepareSkill") })
}

/**
 * Meta task to package all platform skill bundles (with ZIP).
 * This is the main entry point for creating release artifacts.
 */
tasks.register("packageAllSkills") {
    group = "distribution"
    description = "Packages skill bundle ZIPs for all platforms"

    // Dynamically depend on all packageSkill tasks
    dependsOn(tasks.matching { it.name.startsWith("packageSkill") })

    doLast {
        val skillsDirLayout = layout.buildDirectory.dir("skills").get()
        val skillsDir = skillsDirLayout.asFile
        val skillsReleasesDir = layout.buildDirectory.dir("skillsReleases").get().asFile

        if (skillsDir.exists()) {
            logger.lifecycle("✓ All skill packages created successfully!")
            logger.lifecycle("  Location: ${skillsDir.absolutePath}")

            val packagingTasks = tasks.withType<SkillPackagingTask>()
            val skillDirs = packagingTasks.mapNotNull {
                it.outputDir.asFile.orNull
            }
            logger.lifecycle("  Total packages: ${skillDirs.size}")

            for (file in skillDirs) {
                logger.lifecycle("    - ${file.absolutePath}")
            }

            val pluginDir = skillsDirLayout.dir(".claude-plugin")
            pluginDir.asFile.mkdirs()
            val marketplaceJsonFile = pluginDir.file("marketplace.json").asFile
            marketplaceJsonFile.writeText(marketPlaceJson(project.version.toString(), skillDirs.map { it.name }))
        }

        // Copy and rename executables to skillsReleases directory
        if (skillsReleasesDir.exists()) {
            logger.lifecycle("\n✓ Preparing standalone executables for release...")
            logger.lifecycle("  Location: ${skillsReleasesDir.absolutePath}")

            var copiedCount = 0
            var skippedCount = 0

            kotlin.targets.withType<KotlinNativeTarget> {
                val releaseBinary = binaries.findExecutable(NativeBuildType.RELEASE)
                if (releaseBinary != null) {
                    val targetName = name // e.g., "macosX64"
                    val platformName = targetName.lowercase() // e.g., "macosx64"
                    val outputFile = releaseBinary.outputFile

                    if (outputFile.exists()) {
                        val extension = if (targetName.equals("mingwX64", true)) ".exe" else ""
                        val targetFileName = "codex-kkp-cli-$platformName$extension"
                        val targetFile = File(skillsReleasesDir, targetFileName)

                        outputFile.copyTo(targetFile, overwrite = true)
                        logger.lifecycle("    ✓ $targetFileName (${targetFile.length() / 1024} KB)")
                        copiedCount++
                    } else {
                        logger.lifecycle("    ⊘ codex-kkp-cli-$platformName - not available on this host OS")
                        skippedCount++
                    }
                }
            }

            logger.lifecycle("\n✓ Release artifacts summary:")
            logger.lifecycle("  ZIP packages: ${skillsReleasesDir.listFiles { f -> f.extension == "zip" }?.size ?: 0}")
            logger.lifecycle("  Executables: $copiedCount created" + if (skippedCount > 0) ", $skippedCount skipped" else "")
        }
    }
}

private fun marketPlaceJson(version: String, skillDirList: List<String>): String {
    // language=JSON
    return """
        {
          "name": "codex-agent-collaboration-marketplace",
          "owner": {
            "name": "Forte Scarlet",
            "email": "ForteScarlet@163.com"
          },
          "metadata": {
            "description": "Marketplace for codex agent collaboration skills",
            "version": "$version"
          },
          "plugins": [
            {
              "name": "codex-agent-collaboration-skills",
              "description": "Execute tasks using Codex AI Agent for code analysis, implementation, and collaboration",
              "version": "$version"
              "source": "./",
              "strict": false,
              "skills": [${skillDirList.joinToString(separator = ",") { "\"./${it}\"" }}]
            }
          ]
        }
    """.trimIndent()
}
