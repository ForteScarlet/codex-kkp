package love.forte.tools.codexkkp.command

import kotlinx.cinterop.*
import platform.posix._pclose
import platform.posix._popen
import platform.posix.fgets
import platform.posix.memset
import platform.windows.*

@OptIn(ExperimentalForeignApi::class)
actual fun executeCommand(command: String): CommandResult = memScoped {
    // 创建安全属性，允许句柄继承
    val saAttr = alloc<SECURITY_ATTRIBUTES>().apply {
        nLength = sizeOf<SECURITY_ATTRIBUTES>().toUInt()
        bInheritHandle = 1
        lpSecurityDescriptor = null
    }

    // 创建 stdout 管道
    val stdoutRead = alloc<HANDLEVar>()
    val stdoutWrite = alloc<HANDLEVar>()
    if (CreatePipe(stdoutRead.ptr, stdoutWrite.ptr, saAttr.ptr, 0u) == 0) {
        throw RuntimeException("Failed to create stdout pipe")
    }

    // 创建 stderr 管道
    val stderrRead = alloc<HANDLEVar>()
    val stderrWrite = alloc<HANDLEVar>()
    if (CreatePipe(stderrRead.ptr, stderrWrite.ptr, saAttr.ptr, 0u) == 0) {
        CloseHandle(stdoutRead.value)
        CloseHandle(stdoutWrite.value)
        throw RuntimeException("Failed to create stderr pipe")
    }

    // 确保读取端不被继承
    SetHandleInformation(stdoutRead.value, HANDLE_FLAG_INHERIT.toUInt(), 0u)
    SetHandleInformation(stderrRead.value, HANDLE_FLAG_INHERIT.toUInt(), 0u)

    // 设置启动信息
    val startupInfo = alloc<STARTUPINFOW>().apply {
        memset(ptr, 0, sizeOf<STARTUPINFOW>().toULong())
        cb = sizeOf<STARTUPINFOW>().toUInt()
        dwFlags = STARTF_USESTDHANDLES.toUInt()
        hStdOutput = stdoutWrite.value
        hStdError = stderrWrite.value
        hStdInput = GetStdHandle(STD_INPUT_HANDLE)
    }

    // 创建进程信息结构
    val processInfo = alloc<PROCESS_INFORMATION>()

    // 构建命令行（使用 cmd.exe 执行）
    val cmdLine = "cmd.exe /C $command"
    val cmdLineW = cmdLine.wcstr.getPointer(this)

    // 创建进程
    val success = CreateProcessW(
        lpApplicationName = null,
        lpCommandLine = cmdLineW,
        lpProcessAttributes = null,
        lpThreadAttributes = null,
        bInheritHandles = 1,
        dwCreationFlags = 0u,
        lpEnvironment = null,
        lpCurrentDirectory = null,
        lpStartupInfo = startupInfo.ptr,
        lpProcessInformation = processInfo.ptr
    )

    // 关闭写端（父进程不需要）
    CloseHandle(stdoutWrite.value)
    CloseHandle(stderrWrite.value)

    if (success == 0) {
        CloseHandle(stdoutRead.value)
        CloseHandle(stderrRead.value)
        throw RuntimeException("Failed to create process")
    }

    // 读取输出
    val stdout = readFromHandle(stdoutRead.value)
    val stderr = readFromHandle(stderrRead.value)

    // 等待进程结束
    WaitForSingleObject(processInfo.hProcess, INFINITE)

    // 获取退出码
    val exitCodeVar = alloc<UIntVar>()
    GetExitCodeProcess(processInfo.hProcess, exitCodeVar.ptr)
    val exitCode = exitCodeVar.value.toInt()

    // 清理
    CloseHandle(processInfo.hProcess)
    CloseHandle(processInfo.hThread)
    CloseHandle(stdoutRead.value)
    CloseHandle(stderrRead.value)

    CommandResult(exitCode, stdout, stderr)
}

@OptIn(ExperimentalForeignApi::class)
private fun readFromHandle(handle: HANDLE?): String {
    if (handle == null) return ""

    val buffer = ByteArray(4096)
    return buildString {
        buffer.usePinned { pinned ->
            memScoped {
                val bytesRead = alloc<UIntVar>()
                while (true) {
                    val success = ReadFile(
                        hFile = handle,
                        lpBuffer = pinned.addressOf(0).reinterpret(),
                        nNumberOfBytesToRead = buffer.size.toUInt(),
                        lpNumberOfBytesRead = bytesRead.ptr,
                        lpOverlapped = null
                    )
                    if (success == 0 || bytesRead.value == 0u) break
                    append(buffer.decodeToString(0, bytesRead.value.toInt()))
                }
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun executeCommandSimple(command: String): SimpleCommandResult = memScoped {
    val fp = _popen(command, "r") ?: throw RuntimeException("Failed to run command: $command")

    try {
        val buffer = ByteArray(4096)
        val output = buildString {
            while (true) {
                val input = fgets(buffer.refTo(0), buffer.size, fp) ?: break
                append(input.toKString())
            }
        }

        val exitCode = _pclose(fp)

        SimpleCommandResult(exitCode, output)
    } catch (e: Exception) {
        _pclose(fp)
        throw e
    }
}
