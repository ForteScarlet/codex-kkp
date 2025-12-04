package love.forte.tools.codexkkp.sdk.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus

/**
 * Codex JSON 解析器和工具函数
 */
object CodexParser {
    /**
     * JSON 序列化配置
     * - ignoreUnknownKeys: 忽略未知字段，提升容错性
     * - isLenient: 允许宽松的 JSON 格式
     */
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false

        serializersModule += SerializersModule {
            polymorphicDefaultDeserializer(CodexEvent::class) {
                UnknownEvent.serializer()
            }

            polymorphicDefaultDeserializer(CodexItem::class) {
                UnknownItem.serializer()
            }
        }

    }

    /**
     * 解析单行 JSON 事件
     * @param line JSON 行
     * @return 解析的事件，失败返回 null
     */
    fun parseEvent(line: String): CodexEvent? {
        return try {
            val raw = line.trim()
            json.decodeFromString<CodexEvent>(raw).also {
                (it as? UnknownEvent)?.raw = raw
            }
        } catch (_: Exception) {
            // TODO error? log?
            null
        }
    }

    /**
     * 解析事件流（多行 JSON）
     * @param output JSON Lines 格式输出
     * @return 解析的事件列表
     */
    fun parseEventStream(output: String): List<CodexEvent> {
        return output.lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { parseEvent(it) }
            .toList()
    }
}

/**
 * 按类型过滤事件
 */
inline fun <reified T : CodexEvent> List<CodexEvent>.filterByType(): List<T> {
    return filterIsInstance<T>()
}

/**
 * 提取所有 Agent 消息
 */
fun List<CodexEvent>.getAgentMessages(): List<String> {
    return filterIsInstance<ItemCompletedEvent>()
        .mapNotNull { it.item as? AgentMessageItem }
        .map { it.text }
}

/**
 * 提取 Token 使用统计
 */
fun List<CodexEvent>.getUsage(): Usage? {
    return filterIsInstance<TurnCompletedEvent>()
        .firstOrNull()?.usage
}
