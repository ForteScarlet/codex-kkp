package love.forte.tools.codexkkp.sdk

/**
 * Codex 命令构建器（内部使用）
 *
 * 将 [CodexExecConfig] 转换为命令行参数字符串。
 */
object CodexCommandBuilder {

    /**
     * 构建完整命令字符串
     *
     * @param task 执行任务描述
     * @param config 配置
     * @return 完整命令行字符串
     */
    fun build(task: String, config: CodexExecConfig): String {
        return buildList {
            add("codex")
            add("exec")

            // --json 始终添加（内部实现，不公开）
            add("--json")

            // --cd=<dir> 工作目录（必填）
            add("--cd")
            add(escapeArg(config.workingDirectory))

            if (config.fullAuto) {
                add("--full-auto")
            }

            config.sandbox?.let {
                add("--sandbox")
                add(it.value)
            }

            config.outputLastMessage?.let {
                add("--output-last-message")
                add(escapeArg(it))
            }

            config.outputSchema?.let {
                add("--output-schema")
                add(escapeArg(it))
            }

            if (config.skipGitRepoCheck) {
                add("--skip-git-repo-check")
            }

            // --image=<path> 图片路径（可多个）
            for (image in config.images) {
                add("--image")
                add(escapeArg(image))
            }

            // --session=<id>
            config.session?.let {
                add("--session")
                add(escapeArg(it))
            }

            // 任务参数（最后添加）
            add(escapeArg(task))
        }.joinToString(" ")
    }

    /**
     * 转义参数以便在 shell 中安全使用
     *
     * 处理规则：
     * - 空参数返回空引号 `""`
     * - 包含空格、引号、反斜杠或其他 shell 特殊字符的参数会被引号包裹
     * - 引号内的反斜杠和双引号会被转义
     *
     * @param arg 原始参数
     * @return 转义后的参数
     */
    internal fun escapeArg(arg: String): String {
        // 空参数需要用引号表示
        if (arg.isEmpty()) {
            return "\"\""
        }

        // 检查是否需要用引号包裹
        val needsQuoting = arg.any { ch ->
            ch == ' ' || ch == '"' || ch == '\\' ||
                ch == '\'' || ch == '$' || ch == '`' ||
                ch == '!' || ch == '*' || ch == '?' ||
                ch == '[' || ch == ']' || ch == '{' || ch == '}' ||
                ch == '(' || ch == ')' || ch == '<' || ch == '>' ||
                ch == '|' || ch == '&' || ch == ';' || ch == '\n' ||
                ch == '\t' || ch == '\r'
        }

        return if (needsQuoting) {
            // 先转义反斜杠，再转义双引号
            "\"${arg.replace("\\", "\\\\").replace("\"", "\\\"")}\""
        } else {
            arg
        }
    }
}
