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
 * Unified skill packaging for all platforms.
 *
 * Creates a single skill directory containing executables for all platforms,
 * rather than separate platform-specific skill directories.
 */

val unifiedSkillDir = layout.buildDirectory.dir("skills/codex-agent-collaboration")
val skillsReleasesDir = layout.buildDirectory.dir("skillsReleases")

// Unified preparation task
val prepareUnifiedSkill = tasks.register<UnifiedSkillPackagingTask>("prepareUnifiedSkill") {
    group = "distribution"
    description = "Prepares unified skill bundle with all platform executables"

    // Set executables source directory
    this.executablesSourceDir.set(layout.buildDirectory.dir("bin"))

    // Set platform mapping (target name -> platform name)
    val platforms = kotlin.targets.withType<KotlinNativeTarget>().associate { target ->
        target.name to target.name.lowercase()
    }
    this.platformMapping.set(platforms)

    this.templateDir.set(rootProject.file("claude-code-skills-template/codex-agent-collaboration"))
    this.version.set(project.version.toString())
    this.outputDir.set(unifiedSkillDir)

    // Depend on all platform link tasks
    dependsOn(kotlin.targets.withType<KotlinNativeTarget>()
        .mapNotNull { it.binaries.findExecutable(NativeBuildType.RELEASE)?.linkTaskProvider })
}

// Unified ZIP packaging task
tasks.register<Zip>("packageUnifiedSkill") {
    group = "distribution"
    description = "Packages unified skill bundle ZIP with all platforms"

    dependsOn(prepareUnifiedSkill)

    from(prepareUnifiedSkill.flatMap { it.outputDir }) {
        into("codex-agent-collaboration")
    }

    archiveBaseName.set("codex-agent-collaboration")
    archiveVersion.set("")
    archiveExtension.set("zip")
    destinationDirectory.set(skillsReleasesDir)

    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    doLast {
        logger.lifecycle("Created: ${archiveFile.get().asFile.name} (${archiveFile.get().asFile.length() / 1024} KB)")
    }
}

/**
 * Meta task to prepare the unified skill bundle (without ZIP).
 */
tasks.register("prepareAllSkills") {
    group = "distribution"
    description = "Prepares unified skill bundle with all platform executables"

    dependsOn("prepareUnifiedSkill")
}

/**
 * Meta task to package the unified skill bundle and standalone executables.
 * This is the main entry point for creating release artifacts.
 */
tasks.register("packageAllSkills") {
    group = "distribution"
    description = "Packages unified skill bundle ZIP and standalone executables"

    // Dynamically depend on the unified package task
    dependsOn("packageUnifiedSkill")

    doLast {
        val skillsDirLayout = layout.buildDirectory.dir("skills").get()
        val skillsDir = skillsDirLayout.asFile
        val skillsReleasesDir = layout.buildDirectory.dir("skillsReleases").get().asFile

        if (skillsDir.exists()) {
            logger.lifecycle("✓ Unified skill package created successfully!")
            logger.lifecycle("  Location: ${skillsDir.absolutePath}")

            val skillDir = skillsDirLayout.dir("codex-agent-collaboration").asFile
            val executablesDir = skillDir.resolve("executables")
            val executables = executablesDir.listFiles()?.filter { it.canExecute() } ?: emptyList()
            logger.lifecycle("  Platforms included: ${executables.size}")

            for (file in executables) {
                logger.lifecycle("    - ${file.name} (${file.length() / 1024} KB)")
            }

            // Generate marketplace.json for the unified skill
            val pluginDir = skillsDirLayout.dir(".claude-plugin")
            pluginDir.asFile.mkdirs()
            val marketplaceJsonFile = pluginDir.file("marketplace.json").asFile
            marketplaceJsonFile.writeText(marketPlaceJson(project.version.toString()))
        }

        // Copy standalone executables to skillsReleases directory
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
                        // Uniform naming: codex-kkp-cli-{platform} (no .exe extension)
                        val targetFileName = "codex-kkp-cli-$platformName"
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
