package love.forte.tools.codexkkp.sdk

import love.forte.tools.codexkkp.command.executeCommandSimple
import love.forte.tools.codexkkp.sdk.model.CodexEvent
import love.forte.tools.codexkkp.sdk.model.CodexParser
import love.forte.tools.codexkkp.sdk.model.getAgentMessages

/**
 * Codex CLI 封装
 *
 * 提供 `codex exec` 命令的 Kotlin API。
 */
class Codex {

    /**
     * 执行 Codex 任务并返回原始 JSON Lines 输出
     *
     * @param task 任务描述
     * @param config 配置
     * @return JSON Lines 格式原始输出
     */
    fun execRaw(task: String, config: CodexExecConfig): String {
        val command = CodexCommandBuilder.build(task, config)
        val result = executeCommandSimple(command)
        return result.output
    }

    /**
     * 执行 Codex 任务并解析事件流
     *
     * @param task 任务描述
     * @param config 配置
     * @return 解析后的事件列表
     */
    fun exec(task: String, config: CodexExecConfig): List<CodexEvent> {
        val output = execRaw(task, config)
        return CodexParser.parseEventStream(output)
    }

    /**
     * 执行 Codex 任务并返回 Agent 消息
     *
     * @param task 任务描述
     * @param config 配置
     * @return Agent 消息文本列表
     */
    fun execForMessages(task: String, config: CodexExecConfig): List<String> {
        return exec(task, config).getAgentMessages()
    }

    /**
     * 使用 DSL 配置执行任务
     *
     * 示例:
     * ```kotlin
     * codex.exec("修复 Bug") {
     *     workingDirectory("/path/to/project")
     *     fullAuto()
     *     skipGitRepoCheck()
     * }
     * ```
     */
    inline fun exec(task: String, configBlock: CodexExecConfigBuilder.() -> Unit): List<CodexEvent> {
        val config = codexConfig(configBlock)
        return exec(task, config)
    }

    /**
     * 使用 DSL 配置执行任务并返回 Agent 消息
     */
    inline fun execForMessages(task: String, configBlock: CodexExecConfigBuilder.() -> Unit): List<String> {
        val config = codexConfig(configBlock)
        return execForMessages(task, config)
    }
}
