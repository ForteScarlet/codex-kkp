package love.forte.tools.codexkkp.cli

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.fprintf
import platform.posix.stderr

@OptIn(ExperimentalForeignApi::class)
internal actual fun printError(message: String) {
    fprintf(stderr, "%s\n", message)
}