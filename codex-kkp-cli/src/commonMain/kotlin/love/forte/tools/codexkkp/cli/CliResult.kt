package love.forte.tools.codexkkp.cli

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * CLI 返回给调用者的统一 JSON 格式。
 *
 * @author ForteScarlet
 */
@Serializable
class CliResult private constructor(
    /**
     * 结果的类型。
     */
    val type: CliResultType,
    /**
     * 会话ID
     */
    val session: String? = null,
    /**
     * 响应内容。
     */
    val content: JsonElement? = null,
) {
    companion object {
        internal fun success(session: String?, content: JsonElement?): CliResult {
            return CliResult(
                CliResultType.SUCCESS,
                session,
                content,
            )
        }

        internal fun failure(content: JsonElement?): CliResult {
            return CliResult(
                CliResultType.ERROR,
                session = null,
                content = content,
            )
        }
    }

}

enum class CliResultType {
    SUCCESS, ERROR
}