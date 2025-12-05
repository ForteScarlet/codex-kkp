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
 * Unified plugin packaging for all platforms.
 *
 * Creates a single plugin directory containing executables for all platforms,
 * rather than separate platform-specific plugin directories.
 */

val unifiedPluginDir = layout.buildDirectory.dir("plugins/codex-agent-collaboration")
val pluginsReleasesDir = layout.buildDirectory.dir("pluginsReleases")

// Marketplace packaging directories
val marketplaceDir = layout.buildDirectory.dir("marketplace")

// Unified preparation task
val prepareUnifiedSkill = tasks.register<UnifiedSkillPackagingTask>("prepareUnifiedSkill") {
    group = "distribution"
    description = "Prepares unified plugin bundle with all platform executables"

    // Set executables source directory
    this.executablesSourceDir.set(layout.buildDirectory.dir("bin"))

    // Set platform mapping (target name -> platform name)
    val platforms = kotlin.targets.withType<KotlinNativeTarget>().associate { target ->
        target.name to target.name.lowercase()
    }
    this.platformMapping.set(platforms)

    this.templateDir.set(rootProject.file("claude-code-skills-template/codex-agent-collaboration"))
    this.version.set(project.version.toString())
    this.outputDir.set(unifiedPluginDir)

    // Depend on all platform link tasks
    dependsOn(kotlin.targets.withType<KotlinNativeTarget>()
        .mapNotNull { it.binaries.findExecutable(NativeBuildType.RELEASE)?.linkTaskProvider })
}

// Unified ZIP packaging task
tasks.register<Zip>("packageUnifiedSkill") {
    group = "distribution"
    description = "Packages unified plugin bundle ZIP with all platforms"

    dependsOn(prepareUnifiedSkill)

    from(prepareUnifiedSkill.flatMap { it.outputDir }) {
        into("codex-agent-collaboration")
    }

    archiveBaseName.set("codex-agent-collaboration")
    archiveVersion.set("")
    archiveExtension.set("zip")
    destinationDirectory.set(pluginsReleasesDir)

    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    doLast {
        logger.lifecycle("Created: ${archiveFile.get().asFile.name} (${archiveFile.get().asFile.length() / 1024} KB)")
    }
}

/**
 * Marketplace packaging for complete plugin structure.
 *
 * Creates a marketplace directory with proper plugin hierarchy including
 * agents, skills, and metadata files.
 */

// Marketplace preparation task
val prepareMarketplace = tasks.register<MarketplacePackagingTask>("prepareMarketplace") {
    group = "distribution"
    description = "Prepares complete marketplace structure with plugin, agents, and skills"

    // Depend on skill packaging (reuse existing task output)
    dependsOn(prepareUnifiedSkill)
    this.skillSourceDir.set(prepareUnifiedSkill.flatMap { it.outputDir })

    // Agent templates
    this.agentTemplateDir.set(rootProject.file("claude-code-skills-template/agents"))

    // Marketplace metadata
    this.marketplaceName.set("codex-agent-collaboration-marketplace")
    this.version.set(project.version.toString())
    this.ownerName.set("Forte Scarlet")
    this.ownerEmail.set("ForteScarlet@163.com")

    // Plugin metadata
    this.pluginName.set("codex-agent-collaboration-plugin")
    this.pluginDescription.set("Execute tasks using Codex AI agent for code analysis, implementation, and collaboration")
    this.authorName.set("Forte Scarlet")
    this.authorEmail.set("ForteScarlet@163.com")

    // Agent paths (relative to plugin root)
    this.agentPaths.set(listOf("./agents/codex-collaboration.md"))

    // Output location
    this.outputDir.set(marketplaceDir)
}

// Marketplace ZIP packaging task
tasks.register<Zip>("packageMarketplace") {
    group = "distribution"
    description = "Packages complete marketplace bundle ZIP with plugins, agents, skills, and metadata"

    dependsOn(prepareMarketplace)

    from(prepareMarketplace.flatMap { it.outputDir }) {
        // Include hidden files (.claude-plugin directories)
        include("**")
    }

    // Enable inclusion of hidden files and directories
    setIncludeEmptyDirs(true)

    archiveBaseName.set("codex-agent-collaboration-marketplace")
    archiveVersion.set("")
    archiveExtension.set("zip")
    destinationDirectory.set(pluginsReleasesDir)

    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    doLast {
        logger.lifecycle("Created: ${archiveFile.get().asFile.name} (${archiveFile.get().asFile.length() / 1024} KB)")
    }
}

/**
 * Meta task to prepare the unified plugin bundle and marketplace (without ZIP).
 */
tasks.register("prepareAllSkills") {
    group = "distribution"
    description = "Prepares unified plugin bundle and marketplace structure with all platform executables"

    dependsOn("prepareUnifiedSkill", "prepareMarketplace")
}

/**
 * Meta task to package the unified plugin bundle, marketplace, and standalone executables.
 * This is the main entry point for creating release artifacts.
 */
tasks.register("packageAllSkills") {
    group = "distribution"
    description = "Packages unified plugin bundle ZIP, marketplace ZIP, and standalone executables"

    // Dynamically depend on the unified package task and marketplace package task
    dependsOn("packageUnifiedSkill", "packageMarketplace")

    doLast {
        val pluginsDirLayout = layout.buildDirectory.dir("plugins").get()
        val pluginsDir = pluginsDirLayout.asFile
        val pluginsReleasesDir = layout.buildDirectory.dir("pluginsReleases").get().asFile

        if (pluginsDir.exists()) {
            logger.lifecycle("✓ Unified plugin package created successfully!")
            logger.lifecycle("  Location: ${pluginsDir.absolutePath}")

            val pluginDir = pluginsDirLayout.dir("codex-agent-collaboration").asFile
            val executablesDir = pluginDir.resolve("executables")
            val executables = executablesDir.listFiles()?.filter { it.canExecute() } ?: emptyList()
            logger.lifecycle("  Platforms included: ${executables.size}")

            for (file in executables) {
                logger.lifecycle("    - ${file.name} (${file.length() / 1024} KB)")
            }

            // Generate marketplace.json for the unified plugin
            val pluginMetadataDir = pluginsDirLayout.dir(".claude-plugin")
            pluginMetadataDir.asFile.mkdirs()
            val marketplaceJsonFile = pluginMetadataDir.file("marketplace.json").asFile
            marketplaceJsonFile.writeText(marketPlaceJson(project.version.toString()))
        }

        // Copy standalone executables to pluginsReleases directory
        if (pluginsReleasesDir.exists()) {
            logger.lifecycle("\n✓ Preparing standalone executables for release...")
            logger.lifecycle("  Location: ${pluginsReleasesDir.absolutePath}")

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
                        val targetFile = File(pluginsReleasesDir, targetFileName)

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
            logger.lifecycle("  ZIP packages: ${pluginsReleasesDir.listFiles { f -> f.extension == "zip" }?.size ?: 0}")
            logger.lifecycle("    - codex-agent-collaboration.zip (plugin only)")
            logger.lifecycle("    - codex-agent-collaboration-marketplace.zip (complete marketplace)")
            logger.lifecycle("  Executables: $copiedCount created" + if (skippedCount > 0) ", $skippedCount skipped" else "")
        }

        // Log marketplace structure info
        val marketplaceDirLayout = layout.buildDirectory.dir("marketplace").get()
        val marketplaceDirFile = marketplaceDirLayout.asFile

        if (marketplaceDirFile.exists()) {
            logger.lifecycle("\n✓ Marketplace package created successfully!")
            logger.lifecycle("  Location: ${marketplaceDirFile.absolutePath}")

            val pluginDir = marketplaceDirFile.listFiles()?.firstOrNull { it.isDirectory && it.name.endsWith("-plugin") }
            if (pluginDir != null) {
                val pluginAgentsDir = pluginDir.resolve("agents")
                val pluginSkillsDir = pluginDir.resolve("skills")

                val agentCount = pluginAgentsDir.listFiles()?.count { it.extension == "md" } ?: 0
                val skillCount = pluginSkillsDir.listFiles()?.count { it.isDirectory } ?: 0

                logger.lifecycle("  Structure:")
                logger.lifecycle("    - .claude-plugin/marketplace.json")
                logger.lifecycle("    - " + pluginDir.name + "/")
                logger.lifecycle("      - .claude-plugin/plugin.json")
                logger.lifecycle("      - agents/ ($agentCount agents)")
                logger.lifecycle("      - skills/ ($skillCount skills)")
            }
        }
    }
}
