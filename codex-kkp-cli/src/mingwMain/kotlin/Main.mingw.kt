import kotlinx.cinterop.*
import platform.windows.*


/**
 * Windows 平台入口点处理函数
 *
 * 解决 Kotlin/Native 在 mingw 平台上接收命令行参数乱码的问题。
 *
 * 问题根源：
 * - Kotlin/Native 的 main(args: Array<String>) 使用 ANSI 版本入口点
 * - Windows 用系统代码页（如 GBK/CP936）解码参数
 * - 现代终端（Windows Terminal、PowerShell 7、Git Bash）以 UTF-8 传递参数
 * - UTF-8 字节被当作 GBK 解码导致乱码（"锟斤拷"）
 *
 * 解决方案：
 * - 使用 GetCommandLineW() 获取原始 UTF-16 命令行
 * - 使用 CommandLineToArgvW() 解析参数
 * - 使用 toKStringFromUtf16() 直接转换为 Kotlin String
 *   （K/N 内部也使用 UTF-16，所以这是最高效的方式）
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun topMain(args: Array<String>): Array<String> {
    SetConsoleCP(CP_UTF8.toUInt())
    SetConsoleOutputCP(CP_UTF8.toUInt())

    return getUnicodeArgs()
}

/**
 * 使用 Windows Wide Character API 获取 Unicode 命令行参数
 * 绕过 ANSI 代码页转换问题
 */
@OptIn(ExperimentalForeignApi::class)
private fun getUnicodeArgs(): Array<String> = memScoped {
    // 获取原始的 UTF-16 命令行
    val commandLine = GetCommandLineW() ?: return@memScoped emptyArray()

    // 解析命令行为参数数组
    val argc = alloc<IntVar>()
    val argv = CommandLineToArgvW(commandLine.toKString(), argc.ptr)
        ?: return@memScoped emptyArray()

    try {
        val argCount = argc.value
        if (argCount <= 1) {
            // 只有程序名，没有其他参数
            return@memScoped emptyArray()
        }

        // 转换参数（跳过第一个参数即程序名）
        // 直接使用 toKStringFromUtf16()，因为 K/N String 内部也是 UTF-16
        Array(argCount - 1) { index ->
            argv[index + 1]?.toKStringFromUtf16() ?: ""
        }
    } finally {
        LocalFree(argv)
    }
}
