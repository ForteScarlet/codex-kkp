package love.forte.tools.codexkkp.common.command

/**
 * 命令执行结果
 * @property exitCode 退出码
 * @property stdout 标准输出
 * @property stderr 标准错误输出
 */
data class CommandResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
)

/**
 * 执行命令行并获取输出
 * @param command 要执行的命令
 * @return 命令执行结果，包含退出码、标准输出和错误输出
 */
expect fun executeCommand(command: String): CommandResult

/**
 * 简单命令执行结果（stdout/stderr 合并输出）
 * @property exitCode 退出码
 * @property output 合并的输出内容
 */
data class SimpleCommandResult(
    val exitCode: Int,
    val output: String
)

/**
 * 执行命令并获取合并输出（stdout + stderr）
 * @param command 要执行的命令
 * @return 简单命令执行结果
 */
expect fun executeCommandSimple(command: String): SimpleCommandResult
