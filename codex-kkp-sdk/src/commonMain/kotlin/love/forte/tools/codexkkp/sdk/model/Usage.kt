package love.forte.tools.codexkkp.sdk.model

import kotlinx.serialization.*

/**
 * Token 使用统计
 * @property inputTokens 输入 token 数量
 * @property cachedInputTokens 缓存的输入 token 数量
 * @property outputTokens 输出 token 数量
 */
@Serializable
data class Usage(
    @SerialName("input_tokens") val inputTokens: Int,
    @SerialName("cached_input_tokens") val cachedInputTokens: Int = 0,
    @SerialName("output_tokens") val outputTokens: Int
)
