package love.forte.tools.codexkkp.cli

import love.forte.tools.codexkkp.sdk.CodexExecConfig
import love.forte.tools.codexkkp.sdk.SandboxMode

/**
 * 命令行参数解析结果
 *
 * @property task 任务描述（位置参数）
 * @property config 解析后的配置
 * @property full 是否返回全量的 events 数据
 */
data class CliArgs(
    val task: String?,
    val config: CodexExecConfig,
    val full: Boolean = false
)

/**
 * 命令行参数解析器
 *
 * 支持参数（等号风格）:
 * - `--cd=<dir>`: 工作目录（必填）
 * - `--full-auto`: 允许编辑文件
 * - `--full`: 返回全量的 events 数据而不是挑选的主要内容
 * - `--sandbox=<mode>`: 沙箱模式
 * - `--output-last-message=<file>`: 输出文件
 * - `--output-schema=<path>`: 输出 Schema
 * - `--skip-git-repo-check`: 跳过 Git 检查
 * - `--session=<id>`: 恢复指定会话
 * - `--image=<path>`: 图片文件路径（可重复）
 * - `<task>`: 任务描述（位置参数）
 */
object CliParser {
    /** 完全自动化选项 */
    private const val OPT_FULL_AUTO = "--full-auto"

    /** 返回全量 events 数据选项 */
    private const val OPT_FULL = "--full"

    /** 跳过 Git 仓库检查选项 */
    private const val OPT_SKIP_GIT_REPO_CHECK = "--skip-git-repo-check"

    /** 跳过 Git 仓库检查选项前缀（带值） */
    private const val OPT_SKIP_GIT_REPO_CHECK_PREFIX = "--skip-git-repo-check="

    /** 工作目录选项前缀 */
    private const val OPT_CD_PREFIX = "--cd="

    /** 沙箱模式选项前缀 */
    private const val OPT_SANDBOX_PREFIX = "--sandbox="

    /** 输出最终消息选项前缀 */
    private const val OPT_OUTPUT_LAST_MESSAGE_PREFIX = "--output-last-message="

    /** 输出 Schema 的 schema JSON 文件路径选项前缀 */
    private const val OPT_OUTPUT_SCHEMA_PREFIX = "--output-schema="

    /** 会话 ID 选项前缀 */
    private const val OPT_SESSION_PREFIX = "--session="

    /** 图片文件路径选项前缀 */
    private const val OPT_IMAGE_PREFIX = "--image="

    /** 选项前缀 */
    private const val OPTION_PREFIX = "-"

    // ==================== 辅助函数 ====================

    /**
     * 解析可能带引号的参数值
     *
     * 支持以下格式：
     * - 无引号值: `value` → `value`
     * - 带引号值: `"value"` → `value`
     * - 带转义引号: `"value with \"escaped\""` → `value with "escaped"`
     * - 带转义反斜杠: `"path\\to\\file"` → `path\to\file`
     *
     * @param value 原始参数值
     * @return 解析后的值
     */
    internal fun parseQuotedValue(value: String): String {
        // 如果不是以引号开头和结尾，直接返回
        if (value.length < 2 || !value.startsWith('"') || !value.endsWith('"')) {
            return value
        }

        // 去除外层引号
        val inner = value.substring(1, value.length - 1)

        // 反转义：处理 \" -> " 和 \\ -> \
        return buildString {
            var i = 0
            while (i < inner.length) {
                if (inner[i] == '\\' && i + 1 < inner.length) {
                    when (inner[i + 1]) {
                        '"' -> {
                            append('"')
                            i += 2
                        }
                        '\\' -> {
                            append('\\')
                            i += 2
                        }
                        else -> {
                            // 保留未知的转义序列
                            append(inner[i])
                            i++
                        }
                    }
                } else {
                    append(inner[i])
                    i++
                }
            }
        }
    }

    // ==================== 解析逻辑 ====================

    /**
     * 解析命令行参数
     *
     * @param args 原始参数数组
     * @return 解析结果
     * @throws IllegalArgumentException 参数格式错误或缺少必填参数
     */
    fun parse(args: Array<String>): CliArgs {
        var task: String? = null
        var workingDirectory: String? = null
        var fullAuto = false
        var full = false
        var sandbox: SandboxMode = SandboxMode.READ_ONLY // default: read-only
        var outputLastMessage: String? = null
        var outputSchema: String? = null
        var skipGitRepoCheck = true
        var session: String? = null
        val images = mutableListOf<String>()

        var i = 0
        while (i < args.size) {
            val arg = args[i]

            when {
                // --cd=<dir>
                arg.startsWith(OPT_CD_PREFIX) -> {
                    workingDirectory = parseQuotedValue(arg.substringAfter("="))
                }

                // --full-auto
                arg == OPT_FULL_AUTO -> fullAuto = true

                // --full
                arg == OPT_FULL -> full = true

                // --skip-git-repo-check=<value> (must check before standalone flag)
                arg.startsWith(OPT_SKIP_GIT_REPO_CHECK_PREFIX) -> {
                    val value = parseQuotedValue(arg.substringAfter("="))
                    skipGitRepoCheck = parseBoolean(value)
                }

                // --skip-git-repo-check (standalone flag for backward compatibility)
                arg == OPT_SKIP_GIT_REPO_CHECK -> skipGitRepoCheck = true

                // --sandbox=<value>
                arg.startsWith(OPT_SANDBOX_PREFIX) -> {
                    val value = parseQuotedValue(arg.substringAfter("="))
                    sandbox = parseSandboxMode(value)
                }

                // --output-last-message=<value>
                arg.startsWith(OPT_OUTPUT_LAST_MESSAGE_PREFIX) -> {
                    outputLastMessage = parseQuotedValue(arg.substringAfter("="))
                }

                // --output-schema=<value>
                arg.startsWith(OPT_OUTPUT_SCHEMA_PREFIX) -> {
                    outputSchema = parseQuotedValue(arg.substringAfter("="))
                }

                // --session=<id>
                arg.startsWith(OPT_SESSION_PREFIX) -> {
                    session = parseQuotedValue(arg.substringAfter("="))
                }

                // --image=<path>
                arg.startsWith(OPT_IMAGE_PREFIX) -> {
                    images.add(parseQuotedValue(arg.substringAfter("=")))
                }

                // 未知选项
                arg.startsWith(OPTION_PREFIX) -> {
                    throw IllegalArgumentException("Unknown option: $arg")
                }

                // 位置参数（task）
                else -> {
                    if (task == null) {
                        task = arg
                    } else {
                        throw IllegalArgumentException("Unexpected argument: $arg")
                    }
                }
            }
            i++
        }

        // 校验必填参数
        if (workingDirectory == null) {
            throw IllegalArgumentException("Missing required option: --cd=<dir>")
        }

        val config = CodexExecConfig(
            workingDirectory = workingDirectory,
            fullAuto = fullAuto,
            sandbox = sandbox,
            outputLastMessage = outputLastMessage,
            outputSchema = outputSchema,
            skipGitRepoCheck = skipGitRepoCheck,
            session = session,
            images = images.toList()
        )

        return CliArgs(task, config, full)
    }

    /**
     * 解析沙箱模式
     *
     * @param value 沙箱模式字符串
     * @return 对应的 SandboxMode
     * @throws IllegalArgumentException 未知的沙箱模式
     */
    private fun parseSandboxMode(value: String): SandboxMode {
        return SandboxMode.entries.find {
            it.value.equals(value, true) || it.name.equals(value, true)
        } ?: throw IllegalArgumentException(
            "Unknown sandbox mode: $value. Valid modes: ${SandboxMode.entries.joinToString { it.value }}"
        )
    }

    /**
     * 解析布尔值
     *
     * @param value 布尔值字符串
     * @return 解析后的布尔值
     * @throws IllegalArgumentException 无效的布尔值
     */
    private fun parseBoolean(value: String): Boolean {
        return when (value.lowercase()) {
            "true" -> true
            "false" -> false
            else -> throw IllegalArgumentException(
                "Invalid boolean value: $value. Expected: true or false"
            )
        }
    }
}
