package love.forte.tools.codexkkp.sdk.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============================================================
// Helper Data Classes
// ============================================================

/**
 * Thread 错误信息
 * @property message 错误消息
 */
@Serializable
data class ThreadError(
    val message: String
)

// ============================================================
// Codex Event Base Class
// ============================================================

/**
 * Codex 事件基类
 * 表示 `codex exec --json` 输出的事件流
 */
@Serializable
sealed class CodexEvent

@Serializable
sealed class CodexItemEvent : CodexEvent() {
    abstract val item: CodexItem
}

// ============================================================
// Event Implementations
// ============================================================

/**
 * 会话开始事件
 * @property threadId 会话 ID，可用于稍后恢复会话
 */
@Serializable
@SerialName("thread.started")
data class ThreadStartedEvent(
    @SerialName("thread_id") val threadId: String
) : CodexEvent()

/**
 * 轮次开始事件
 * 当通过向模型发送新提示来开始一个轮次时发出。
 * 一个轮次包含 agent 处理提示时发生的所有事件。
 */
@Serializable
@SerialName("turn.started")
data object TurnStartedEvent : CodexEvent()

/**
 * Item 开始事件
 * 当新 Item 添加到线程时发出。通常 Item 最初处于"进行中"状态。
 * @property item 开始的 Item
 */
@Serializable
@SerialName("item.started")
data class ItemStartedEvent(
    override val item: CodexItem
) : CodexItemEvent()

/**
 * Item 更新事件
 * 当 Item 被更新时发出。
 * @property item 更新后的 Item
 */
@Serializable
@SerialName("item.updated")
data class ItemUpdatedEvent(
    override val item: CodexItem
) : CodexItemEvent()

/**
 * Item 完成事件
 * 表示 Item 已达到终态——成功或失败。
 * @property item 完成的 Item
 */
@Serializable
@SerialName("item.completed")
data class ItemCompletedEvent(
    override val item: CodexItem
) : CodexItemEvent()

/**
 * 轮次完成事件
 * 当轮次完成时发出。通常在助手响应之后。
 * @property usage Token 使用统计
 */
@Serializable
@SerialName("turn.completed")
data class TurnCompletedEvent(
    val usage: Usage
) : CodexEvent()

/**
 * 轮次失败事件
 * 表示轮次因错误而失败。
 * @property error 错误信息
 */
@Serializable
@SerialName("turn.failed")
data class TurnFailedEvent(
    val error: ThreadError? = null
) : CodexEvent()

/**
 * 线程错误事件
 * 表示由事件流直接发出的不可恢复错误。
 * @property message 错误消息
 */
@Serializable
@SerialName("error")
data class ThreadErrorEvent(
    val message: String
) : CodexEvent()

/**
 * 未知事件（容错处理）
 * @property type 事件类型名称
 * @property raw 原始 JSON 数据
 */
@Serializable
@SerialName("unknown")
data class UnknownEvent(
    val type: String,
) : CodexEvent() {
    @SerialName("_raw")
    var raw: String? = null
        internal set
}
