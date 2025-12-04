package love.forte.tools.codexkkp.sdk

/**
 * Codex exec 命令配置
 *
 * 封装 `codex exec` 支持的所有参数（`--json` 内部默认使用，不公开）。
 *
 * @property workingDirectory Codex 工作目录（对应 `--cd=<dir>`，必填）
 * @property fullAuto 允许编辑文件（对应 `--full-auto`）
 * @property sandbox 沙箱模式（对应 `--sandbox=<mode>`）
 * @property outputLastMessage 最终消息输出文件路径（对应 `--output-last-message=<file>`）
 * @property outputSchema 结构化输出 JSON Schema 路径（对应 `--output-schema=<path>`）
 * @property skipGitRepoCheck 跳过 Git 仓库检查（对应 `--skip-git-repo-check`，默认 true）
 * @property session 会话 ID（对应 `--session=<id>`）
 * @property images 图片文件路径列表（对应 `--image=<path>`，可多个）
 */
data class CodexExecConfig(
    val workingDirectory: String,
    val fullAuto: Boolean = false,
    val sandbox: SandboxMode? = null,
    val outputLastMessage: String? = null,
    val outputSchema: String? = null,
    val skipGitRepoCheck: Boolean = true,
    val session: String? = null,
    val images: List<String> = emptyList()
)
