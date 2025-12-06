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
 * @return JSON string for plugin.json
 */
@Language("JSON")
fun pluginJson(
    name: String,
    version: String,
    description: String,
    authorName: String,
    authorEmail: String,
): String {
    return """
        {
          "name": "$name",
          "version": "$version",
          "description": "$description",
          "license": "MIT",
          "homepage": "https://github.com/ForteScarlet/codex-kkp",
          "repository": "https://github.com/ForteScarlet/codex-kkp",
          "keywords": ["AI", "agent", "codex", "assistant"],
          "author": {
            "name": "$authorName",
            "email": "$authorEmail"
          }
        }
    """.trimIndent()
}