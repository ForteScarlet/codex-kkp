package love.forte.tools.codexkkp.common.command

import kotlinx.cinterop.*
import platform.posix.*

/**
 * 从进程状态中提取退出码
 * 模拟 WIFEXITED 和 WEXITSTATUS 宏的行为
 */
private fun statusExitCode(status: Int): Int =
    if ((status and 0x7f) == 0) (status shr 8) and 0xff else -1

@OptIn(ExperimentalForeignApi::class)
actual fun executeCommand(command: String): CommandResult = memScoped {
    // 创建管道: [0] = read end, [1] = write end
    val stdoutPipe = allocArray<IntVar>(2)
    val stderrPipe = allocArray<IntVar>(2)

    if (pipe(stdoutPipe) != 0 || pipe(stderrPipe) != 0) {
        throw RuntimeException("Failed to create pipes")
    }

    when (val pid = fork()) {
        -1 -> {
            // Fork 失败
            close(stdoutPipe[0]); close(stdoutPipe[1])
            close(stderrPipe[0]); close(stderrPipe[1])
            throw RuntimeException("Failed to fork process")
        }

        0 -> {
            // 子进程
            // 关闭读端
            close(stdoutPipe[0])
            close(stderrPipe[0])

            // 重定向 stdout 和 stderr 到管道的写端
            dup2(stdoutPipe[1], STDOUT_FILENO)
            dup2(stderrPipe[1], STDERR_FILENO)

            // 关闭原始的写端文件描述符
            close(stdoutPipe[1])
            close(stderrPipe[1])

            // 执行命令
            execl("/bin/sh", "sh", "-c", command, null)

            // 如果 execl 返回，说明执行失败
            _exit(127)
            throw RuntimeException("Unreachable")
        }

        else -> {
            // 父进程
            // 关闭写端
            close(stdoutPipe[1])
            close(stderrPipe[1])

            // 读取 stdout
            val stdout = readFromPipe(stdoutPipe[0])

            // 读取 stderr
            val stderr = readFromPipe(stderrPipe[0])

            // 关闭读端
            close(stdoutPipe[0])
            close(stderrPipe[0])

            // 等待子进程结束并获取退出码
            val statusPtr = alloc<IntVar>()
            waitpid(pid, statusPtr.ptr, 0)
            val status = statusPtr.value
            val exitCode = statusExitCode(status)

            CommandResult(exitCode, stdout, stderr)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun readFromPipe(fd: Int): String {
    val buffer = ByteArray(4096)
    return buildString {
        memScoped {
            while (true) {
                val bytesRead = read(fd, buffer.refTo(0), buffer.size.toULong()).toInt()
                if (bytesRead <= 0) break
                append(buffer.decodeToString(0, bytesRead))
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun executeCommandSimple(command: String): SimpleCommandResult = memScoped {
    val fp = popen(command, "r") ?: throw RuntimeException("Failed to run command: $command")

    try {
        val buffer = ByteArray(4096)
        val output = buildString {
            while (true) {
                val input = fgets(buffer.refTo(0), buffer.size, fp) ?: break
                append(input.toKString())
            }
        }

        val status = pclose(fp)
        val exitCode = statusExitCode(status)

        SimpleCommandResult(exitCode, output)
    } catch (e: Exception) {
        pclose(fp)
        throw e
    }
}
