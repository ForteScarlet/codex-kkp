import org.intellij.lang.annotations.Language

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