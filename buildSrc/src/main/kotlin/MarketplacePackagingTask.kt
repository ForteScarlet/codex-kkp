import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import javax.inject.Inject

/**
 * Gradle task to prepare a complete Claude Code plugin marketplace structure.
 *
 * This task creates a marketplace directory that contains multiple plugins, where each plugin
 * has its own metadata, agents, and skills. The structure follows Claude Code conventions:
 *
 * marketplace-root/
 *   ├── .claude-plugin/
 *   │   └── marketplace.json          (marketplace metadata)
 *   └── plugin-name/
 *       ├── .claude-plugin/
 *       │   └── plugin.json            (plugin metadata)
 *       ├── agents/
 *       │   └── agent-name.md          (agent definitions)
 *       └── skills/
 *           └── skill-name/             (skill bundles)
 *               ├── SKILL.md
 *               └── executables/
 *
 * This task follows Gradle best practices:
 * - Uses [FileSystemOperations] for file operations (injected service)
 * - Supports incremental builds via proper input/output annotations
 * - Supports build cache via [@CacheableTask] and [@PathSensitive]
 *
 * ZIP creation is handled separately by a standard [org.gradle.api.tasks.bundling.Zip] task
 * for better separation of concerns.
 *
 * @see UnifiedSkillPackagingTask
 */
@CacheableTask
abstract class MarketplacePackagingTask @Inject constructor(
    private val fsOps: FileSystemOperations
) : DefaultTask() {

    /**
     * Pre-packaged plugin directory from UnifiedSkillPackagingTask.
     * This should point to build/plugins/codex-agent-collaboration/
     */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val skillSourceDir: DirectoryProperty

    /**
     * Agent template directory containing agent definition markdown files.
     * Expected structure: templateDir/{agent-name.md, ...}
     */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val agentTemplateDir: DirectoryProperty

    /**
     * Marketplace name (e.g., "codex-agent-collaboration-marketplace")
     */
    @get:Input
    abstract val marketplaceName: Property<String>

    /**
     * Version string for marketplace and plugin
     */
    @get:Input
    abstract val version: Property<String>

    /**
     * Owner name for marketplace
     */
    @get:Input
    abstract val ownerName: Property<String>

    /**
     * Owner email for marketplace
     */
    @get:Input
    abstract val ownerEmail: Property<String>

    /**
     * Plugin name (e.g., "codex-agent-collaboration-plugin")
     */
    @get:Input
    abstract val pluginName: Property<String>

    /**
     * Plugin description
     */
    @get:Input
    abstract val pluginDescription: Property<String>

    /**
     * Author name for plugin
     */
    @get:Input
    abstract val authorName: Property<String>

    /**
     * Author email for plugin
     */
    @get:Input
    abstract val authorEmail: Property<String>

    /**
     * List of agent file paths relative to plugin root (e.g., ["./agents/codex-collaboration.md"])
     */
    @get:Input
    abstract val agentPaths: ListProperty<String>

    /**
     * Output directory for the complete marketplace bundle.
     * This will contain .claude-plugin/ at root and plugin subdirectories.
     * Example: build/marketplace/
     */
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        group = "distribution"
        description = "Prepares complete Claude Code plugin marketplace structure with plugins, agents, and skills"
    }

    @TaskAction
    fun packageMarketplace() {
        val marketplaceRoot = outputDir.get().asFile
        val skillSource = skillSourceDir.get().asFile
        val agentTemplate = agentTemplateDir.get().asFile
        val marketplaceNameStr = marketplaceName.get()
        val versionStr = version.get()
        val pluginNameStr = pluginName.get()
        val pluginDescStr = pluginDescription.get()

        logger.lifecycle("Preparing marketplace bundle: $marketplaceNameStr")

        // 1. Create marketplace root structure
        val marketplaceMetadataDir = marketplaceRoot.resolve(".claude-plugin")
        val pluginDir = marketplaceRoot.resolve(pluginNameStr)

        marketplaceMetadataDir.mkdirs()
        pluginDir.mkdirs()

        // 2. Generate marketplace.json at root
        val marketplaceJsonFile = marketplaceMetadataDir.resolve("marketplace.json")
        marketplaceJsonFile.writeText(
            marketplaceJsonForRoot(
                name = marketplaceNameStr,
                version = versionStr,
                ownerName = ownerName.get(),
                ownerEmail = ownerEmail.get(),
                pluginName = pluginNameStr,
                pluginDescription = pluginDescStr,
                pluginVersion = versionStr,
                pluginSource = "./$pluginNameStr"
            )
        )
        logger.lifecycle("  ✓ Generated marketplace.json at root")

        // 3. Create plugin directory structure
        val pluginMetadataDir = pluginDir.resolve(".claude-plugin")
        val agentsDir = pluginDir.resolve("agents")
        val skillsDir = pluginDir.resolve("skills")

        pluginMetadataDir.mkdirs()
        agentsDir.mkdirs()
        skillsDir.mkdirs()

        // 4. Generate plugin.json
        val pluginJsonFile = pluginMetadataDir.resolve("plugin.json")
        pluginJsonFile.writeText(
            pluginJson(
                name = pluginNameStr,
                version = versionStr,
                description = pluginDescStr,
                authorName = authorName.get(),
                authorEmail = authorEmail.get()
            )
        )
        logger.lifecycle("  ✓ Generated plugin.json")

        // 5. Copy agent definitions to plugin/agents/
        fsOps.sync {
            from(agentTemplate) {
                include("*.md")
            }
            into(agentsDir)
        }

        val agentFiles = agentsDir.listFiles()?.filter { it.extension == "md" } ?: emptyList()
        logger.lifecycle("  ✓ Synced ${agentFiles.size} agent definition(s)")

        // 6. Copy skill bundle to plugin/skills/{skill-name}/
        val skillName = skillSource.name
        val targetSkillDir = skillsDir.resolve(skillName)

        fsOps.sync {
            from(skillSource)
            into(targetSkillDir)
        }
        logger.lifecycle("  ✓ Synced skill bundle: $skillName")

        // 7. Summary
        logger.lifecycle("\nMarketplace bundle prepared: ${marketplaceRoot.absolutePath}")
        logger.lifecycle("  Structure:")
        logger.lifecycle("    - .claude-plugin/marketplace.json")
        logger.lifecycle("    - $pluginNameStr/")
        logger.lifecycle("      - .claude-plugin/plugin.json")
        logger.lifecycle("      - agents/ (${agentFiles.size} agents)")
        logger.lifecycle("      - skills/$skillName/")
    }
}
