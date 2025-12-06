import org.intellij.lang.annotations.Language

/**
 * Generates marketplace.json for plugin-only structure (backwards compatibility).
 *
 * This version is for the old structure where marketplace.json is in build/plugins/.claude-plugin/
 * and references plugins directly.
 */
@Language("JSON")
fun marketPlaceJson(version: String): String {
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
              "name": "codex-agent-collaboration",
              "description": "Execute tasks using Codex AI assistant for code analysis, implementation, and collaboration",
              "version": "$version",
              "source": "./",
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
 * @param name Marketplace name
 * @param version Marketplace version
 * @param ownerName Owner's display name
 * @param ownerEmail Owner's email address
 * @param pluginName Plugin name
 * @param pluginDescription Plugin description
 * @param pluginVersion Plugin version
 * @param pluginSource Relative path to plugin directory from marketplace root
 * @return JSON string for marketplace.json
 */
@Language("JSON")
fun marketplaceJsonForRoot(
    name: String,
    version: String,
    ownerName: String,
    ownerEmail: String,
    pluginName: String,
    pluginDescription: String,
    pluginVersion: String,
    pluginSource: String
): String {
    return """
        {
          "name": "$name",
          "metadata": {
            "description": "Marketplace for codex agent collaboration plugins",
            "version": "$version"
          },
          "owner": {
            "name": "$ownerName",
            "email": "$ownerEmail"
          },
          "plugins": [
            {
              "name": "$pluginName",
              "source": "$pluginSource"
            }
          ]
        }
    """.trimIndent()
}