package love.forte.tools.codexkkp.sdk.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement

// ============================================================
// Enums
// ============================================================

/**
 * 命令执行状态
 */
@Serializable
enum class CommandExecutionStatus {
    @SerialName("in_progress")
    IN_PROGRESS,

    @SerialName("completed")
    COMPLETED,

    @SerialName("failed")
    FAILED
}

/**
 * 文件变更类型
 */
@Serializable
enum class PatchChangeKind {
    @SerialName("add")
    ADD,

    @SerialName("delete")
    DELETE,

    @SerialName("update")
    UPDATE
}

/**
 * 文件变更应用状态
 */
@Serializable
enum class PatchApplyStatus {
    @SerialName("completed")
    COMPLETED,

    @SerialName("failed")
    FAILED
}

/**
 * MCP 工具调用状态
 */
@Serializable
enum class McpToolCallStatus {
    @SerialName("in_progress")
    IN_PROGRESS,

    @SerialName("completed")
    COMPLETED,

    @SerialName("failed")
    FAILED
}

// ============================================================
// Helper Data Classes
// ============================================================

/**
 * 文件更新变更
 * @property path 文件路径
 * @property kind 变更类型
 */
@Serializable
data class FileUpdateChange(
    val path: String,
    val kind: PatchChangeKind
)

/**
 * MCP 工具调用结果
 * @property content 内容块列表
 * @property structuredContent 结构化内容
 */
@Serializable
data class McpToolResult(
    val content: List<JsonElement> = emptyList(),
    @SerialName("structured_content")
    val structuredContent: JsonElement? = null
)

/**
 * MCP 工具调用错误
 * @property message 错误消息
 */
@Serializable
data class McpToolError(
    val message: String = ""
)

/**
 * Todo 项
 * @property text 任务文本
 * @property completed 是否完成
 */
@Serializable
data class TodoItem(
    val text: String,
    val completed: Boolean = true
)

// ============================================================
// Codex Item Base Class
// ============================================================

/**
 * Codex Item 基类
 * Item 代表 Codex 执行过程中的各种操作项
 */
@Serializable
sealed class CodexItem {
    abstract val id: String
}

// ============================================================
// Item Implementations
// ============================================================

/**
 * 命令执行 Item
 * @property command 执行的命令
 * @property aggregatedOutput 命令运行时捕获的聚合 stdout 和 stderr
 * @property exitCode 命令退出码（命令运行时为空）
 * @property status 执行状态
 */
@Serializable
@SerialName("command_execution")
data class CommandExecutionItem(
    override val id: String,
    val command: String,
    @SerialName("aggregated_output")
    val aggregatedOutput: String = "",
    @SerialName("exit_code")
    val exitCode: Int? = null,
    val status: CommandExecutionStatus = CommandExecutionStatus.FAILED
) : CodexItem()

/**
 * Agent 消息 Item
 * @property text Agent 返回的文本消息（可以是自然语言或请求结构化输出时的 JSON）
 */
@Serializable
@SerialName("agent_message")
data class AgentMessageItem(
    override val id: String,
    val text: String = ""
) : CodexItem()

/**
 * 推理过程 Item
 * @property text 推理内容
 */
@Serializable
@SerialName("reasoning")
data class ReasoningItem(
    override val id: String,
    val text: String = ""
) : CodexItem()

/**
 * 文件变更 Item
 * @property changes 变更列表
 * @property status 变更应用状态
 */
@Serializable
@SerialName("file_change")
data class FileChangeItem(
    override val id: String,
    val changes: List<FileUpdateChange> = emptyList(),
    val status: PatchApplyStatus = PatchApplyStatus.FAILED
) : CodexItem()

/**
 * MCP 工具调用 Item
 * @property server 处理请求的 MCP 服务器名称
 * @property tool 在 MCP 服务器上调用的工具
 * @property arguments 传递给工具调用的参数
 * @property result 成功调用时 MCP 服务器返回的结果
 * @property error 失败调用时报告的错误
 * @property status 工具调用状态
 */
@Serializable
@SerialName("mcp_tool_call")
data class McpToolCallItem(
    override val id: String,
    val server: String = "",
    val tool: String = "",
    val arguments: JsonElement? = null,
    val result: McpToolResult? = null,
    val error: McpToolError? = null,
    val status: McpToolCallStatus = McpToolCallStatus.FAILED
) : CodexItem()

/**
 * Web 搜索 Item
 * @property query 搜索查询
 */
@Serializable
@SerialName("web_search")
data class WebSearchItem(
    override val id: String,
    val query: String = ""
) : CodexItem()

/**
 * 错误 Item（非致命错误）
 * @property message 错误消息
 */
@Serializable
@SerialName("error")
data class ErrorItem(
    override val id: String,
    val message: String = ""
) : CodexItem()

/**
 * 代办列表 Item
 * 跟踪 agent 的运行任务列表。在计划发布时开始，随着步骤变化而更新，在轮次结束时完成。
 * @property items 任务列表
 */
@Serializable
@SerialName("todo_list")
data class TodoListItem(
    override val id: String,
    val items: List<TodoItem> = emptyList()
) : CodexItem()

/**
 * 未知类型 Item（容错处理）
 * @property rawData 原始 JSON 数据
 */
@Serializable
@SerialName("unknown_item")
data class UnknownItem(
    override val id: String
) : CodexItem()
