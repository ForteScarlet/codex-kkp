import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import love.forte.tools.codexkkp.cli.CliParser
import love.forte.tools.codexkkp.cli.printErrorResult
import love.forte.tools.codexkkp.cli.printResult
import love.forte.tools.codexkkp.sdk.Codex
import love.forte.tools.codexkkp.sdk.model.*

internal val json: Json = Json(CodexParser.json) {
    this.explicitNulls = false
    this.encodeDefaults = true
}

fun main(args: Array<String>) {
    // 平台特定参数处理（Windows UTF-16 转换等）
    val processedArgs = topMain(args)

    try {
        val cliArgs = CliParser.parse(processedArgs)

        // 检查任务参数
        val task = cliArgs.task
        if (task == null) {
            printErrorResult("Error: No task prompt specified")
            return
        }

        // 执行任务
        val codex = Codex()
        val events = codex.exec(task, cliArgs.config)

        // 解析返回的 events
        printEventResult(processedArgs.asList(), events, cliArgs.full)

        // 输出 Agent 消息
        // events.getAgentMessages().forEach { message ->
        //     println(message)
        // }

    } catch (e: IllegalArgumentException) {
        printErrorResult("IllegalArgumentException: ${e.message}")
    } catch (e: Exception) {
        printErrorResult("Error: ${e.message}")
    }
}

private fun printEventResult(rawArgs: List<String>, events: Iterable<CodexEvent>, full: Boolean) {
    // 如果需要返回全量数据，直接返回所有 events
    if (full) {
        val allEvents = events.toList()

        // 提取 session
        var session = ""
        var error = false

        for (event in allEvents) {
            when (event) {
                is ThreadStartedEvent -> {
                    session = event.threadId
                }

                is ThreadErrorEvent, is TurnFailedEvent -> {
                    error = true
                }

                else -> {
                    // ignore.
                }
            }
        }

        if (error) {
            printErrorResult(
                json.encodeToJsonElement(
                    serializer = ErrorResultContent.serializer(),
                    value = ErrorResultContent(allEvents)
                )
            )
        } else {
            printResult(
                session = session,
                json.encodeToJsonElement(
                    serializer = SuccessFullResultContent.serializer(),
                    value = SuccessFullResultContent(rawArgs, allEvents)
                )
            )
        }
        return
    }

    // session
    var session = ""
    // agent 消息
    val agentMessagesBuilder = StringBuilder()
    // 文件变更
    val fileChanges = mutableListOf<FileChangeItem>()
    // 非致命错误
    val nonFatalErrors = mutableListOf<ErrorItem>()

    val errorEvents = mutableListOf<CodexEvent>()

    for (event in events) {
        when (event) {
            is ThreadStartedEvent -> {
                session = event.threadId
            }

            is CodexItemEvent -> {
                when (val item = event.item) {
                    is AgentMessageItem -> {
                        agentMessagesBuilder.append(item.text)
                    }

                    is FileChangeItem -> {
                        fileChanges.add(item)
                    }

                    is ErrorItem -> {
                        nonFatalErrors.add(item)
                    }

                    // is McpToolCallItem ->
                    // is ReasoningItem ->
                    // is TodoListItem ->
                    // is WebSearchItem ->
                    // is CommandExecutionItem ->
                    // is UnknownItem ->
                    else -> {
                        // ignore.
                    }
                }
            }

            is ThreadErrorEvent -> {
                errorEvents.add(event)
            }

            is TurnFailedEvent -> {
                errorEvents.add(event)
            }

            // is TurnCompletedEvent ->
            // TurnStartedEvent ->
            else -> {
                // ignore.
            }
        }
    }

    if (errorEvents.isNotEmpty()) {
        printErrorResult(
            json.encodeToJsonElement(
                serializer = ErrorResultContent.serializer(),
                value = ErrorResultContent(errorEvents)
            )
        )
    } else {
        printResult(
            session = session,
            json.encodeToJsonElement(
                serializer = SuccessResultContent.serializer(),
                value = SuccessResultContent(
                    agentMessagesBuilder.toString(),
                    fileChanges.takeIf { it.isNotEmpty() },
                    nonFatalErrors.takeIf { it.isNotEmpty() }
                )
            )
        )
    }
}

@Serializable
private data class SuccessResultContent(
    val agentMessages: String,
    val fileChanges: List<FileChangeItem>?,
    val nonFatalErrors: List<ErrorItem>?
)

@Serializable
private data class SuccessFullResultContent(
    val rawArgs: List<String>,
    val fullEvents: List<CodexEvent>
)

@Serializable
private data class ErrorResultContent(
    val errorEvents: List<CodexEvent>
)

internal expect fun topMain(args: Array<String>): Array<String>
