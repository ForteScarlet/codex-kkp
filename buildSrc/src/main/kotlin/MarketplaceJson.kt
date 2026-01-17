import org.intellij.lang.annotations.Language

/**
 * Generates marketplace.json for plugin-only structure (backwards compatibility).
 *
 * This version is for the old structure where marketplace.json is in build/plugins/.claude-plugin/
 * and references plugins directly.
 *
 * Updated to follow the official Claude Code marketplace.json format.
 */
@Language("JSON")
fun marketPlaceJson(version: String): String {
    return """
        {
          "${"$"}schema": "https://anthropic.com/claude-code/marketplace.schema.json",
          "name": "codex-agent-collaboration-marketplace",
          "version": "$version",
          "description": "Marketplace for Codex AI agent collaboration skills - execute tasks using Codex for code analysis, implementation, and collaboration",
          "owner": {
            "name": "Forte Scarlet",
            "email": "ForteScarlet@163.com"
          },
          "plugins": [
            {
              "name": "codex-agent-collaboration",
              "description": "Execute tasks using Codex AI assistant for code analysis, implementation, and collaboration",
              "version": "$version",
              "author": {
                "name": "Forte Scarlet",
                "email": "ForteScarlet@163.com"
              },
              "source": "./",
              "category": "development",
              "strict": false,
              "skills": ["./codex-agent-collaboration"]
            }
          ]
        }
    """.trimIndent()
}

/**
 * Generates marketplace.json for plugin marketplace structure.
 *
 * This version is for the new structure where marketplace root contains multiple plugins,
 * each with their own plugin.json.
 *
 * Following the official Claude Code marketplace.json format from:
 * https://github.com/anthropics/claude-code/blob/main/.claude-plugin/marketplace.json
 *
 * @param name Marketplace name
 * @param version Marketplace version
 * @param description Marketplace description
 * @param ownerName Owner's display name
 * @param ownerEmail Owner's email address
 * @param pluginName Plugin name
 * @param pluginDescription Plugin description
 * @param pluginVersion Plugin version
 * @param pluginSource Relative path to plugin directory from marketplace root
 * @param pluginCategory Plugin category (e.g., "development", "productivity")
 * @return JSON string for marketplace.json
 */
@Language("JSON")
fun marketplaceJsonForRoot(
    name: String,
    version: String,
    description: String,
    ownerName: String,
    ownerEmail: String,
    pluginName: String,
    pluginDescription: String,
    pluginVersion: String,
    pluginSource: String,
    pluginCategory: String = "development"
): String {
    return """
        {
          "${"$"}schema": "https://anthropic.com/claude-code/marketplace.schema.json",
          "name": "$name",
          "version": "$version",
          "description": "$description",
          "owner": {
            "name": "$ownerName",
            "email": "$ownerEmail"
          },
          "plugins": [
            {
              "name": "$pluginName",
              "description": "$pluginDescription",
              "version": "$pluginVersion",
              "author": {
                "name": "$ownerName",
                "email": "$ownerEmail"
              },
              "source": "$pluginSource",
              "category": "$pluginCategory"
            }
          ]
        }
    """.trimIndent()
}