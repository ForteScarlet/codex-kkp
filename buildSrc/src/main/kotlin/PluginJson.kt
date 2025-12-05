import org.intellij.lang.annotations.Language

/**
 * Generates plugin.json for Claude Code plugin structure.
 *
 * This defines the plugin's metadata and references to agents.
 * Skills are NOT included here - they are auto-discovered from the skills/ directory.
 *
 * @param name Plugin name
 * @param version Plugin version
 * @param description Plugin description
 * @param authorName Author's display name
 * @param authorEmail Author's email address
 * @param agents List of agent file paths relative to plugin root (e.g., "./agents/codex-collaboration.md")
 * @return JSON string for plugin.json
 */
@Language("JSON")
fun pluginJson(
    name: String,
    version: String,
    description: String,
    authorName: String,
    authorEmail: String,
    agents: List<String>
): String {
    val agentsJson = agents.joinToString(", ") { "\"$it\"" }

    return """
        {
          "name": "$name",
          "version": "$version",
          "description": "$description",
          "author": {
            "name": "$authorName",
            "email": "$authorEmail"
          },
          "agents": [
            $agentsJson
          ]
        }
    """.trimIndent()
}