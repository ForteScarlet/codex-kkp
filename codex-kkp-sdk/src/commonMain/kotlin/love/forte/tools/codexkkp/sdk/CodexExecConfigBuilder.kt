package love.forte.tools.codexkkp.sdk

/**
 * [CodexExecConfig] DSL 构建器
 *
 * 使用示例:
 * ```kotlin
 * val config = codexConfig {
 *     workingDirectory("/path/to/project")
 *     fullAuto()
 *     dangerFullAccess()
 *     outputSchema("./schema.json")
 *     image("./screenshot.png")
 * }
 * ```
 */
class CodexExecConfigBuilder {
    private var workingDirectory: String? = null
    private var fullAuto: Boolean = false
    private var sandbox: SandboxMode? = null
    private var outputLastMessage: String? = null
    private var outputSchema: String? = null
    private var skipGitRepoCheck: Boolean = true
    private var session: String? = null
    private val images: MutableList<String> = mutableListOf()

    /** 设置工作目录（必填） */
    fun workingDirectory(path: String) {
        workingDirectory = path
    }

    /** 启用完全自动化模式（允许编辑文件） */
    fun fullAuto() {
        fullAuto = true
    }

    /** 设置沙箱模式 */
    fun sandbox(mode: SandboxMode) {
        sandbox = mode
    }

    /** 启用完全访问模式（编辑 + 网络） */
    fun dangerFullAccess() {
        sandbox = SandboxMode.DANGER_FULL_ACCESS
    }

    /** 设置最终消息输出文件 */
    fun outputLastMessage(path: String) {
        outputLastMessage = path
    }

    /** 设置结构化输出 JSON Schema 路径 */
    fun outputSchema(path: String) {
        outputSchema = path
    }

    /** 跳过 Git 仓库检查 */
    fun skipGitRepoCheck() {
        skipGitRepoCheck = true
    }

    /** 设置是否跳过 Git 仓库检查 */
    fun skipGitRepoCheck(skip: Boolean) {
        skipGitRepoCheck = skip
    }

    /** 执行 Git 仓库检查（不跳过） */
    fun doGitRepoCheck() {
        skipGitRepoCheck = false
    }

    /** 设置会话 ID（用于恢复会话） */
    fun session(id: String) {
        session = id
    }

    /** 添加图片文件路径（可多次调用） */
    fun image(path: String) {
        images.add(path)
    }

    @PublishedApi
    internal fun build(): CodexExecConfig {
        val dir = workingDirectory
            ?: throw IllegalStateException("workingDirectory is required")
        return CodexExecConfig(
            workingDirectory = dir,
            fullAuto = fullAuto,
            sandbox = sandbox,
            outputLastMessage = outputLastMessage,
            outputSchema = outputSchema,
            skipGitRepoCheck = skipGitRepoCheck,
            session = session,
            images = images.toList()
        )
    }
}

/** DSL 入口函数 */
inline fun codexConfig(block: CodexExecConfigBuilder.() -> Unit): CodexExecConfig {
    return CodexExecConfigBuilder().apply(block).build()
}
