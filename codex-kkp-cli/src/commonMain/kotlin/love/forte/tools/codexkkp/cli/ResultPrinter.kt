package love.forte.tools.codexkkp.cli

import json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive


/** 输出到 stderr */
internal expect fun printError(message: String)

/** 输出到 stderr */
internal fun printErrorResult(result: CliResult) {
    printError(json.encodeToString(result))
}

/** 输出到 stderr */
internal fun printErrorResult(content: JsonElement?) {
    printErrorResult(CliResult.failure(content))
}

/**
 * 输出到 stderr
 */
internal fun printErrorResult(content: String?) {
    printErrorResult(CliResult.failure(JsonPrimitive(content)))
}

internal fun printResult(result: CliResult) {
    println(json.encodeToString(result))
}

internal fun printResult(session: String, content: JsonElement?) {
    printResult(CliResult.success(session, content))
}