package codex

import love.forte.tools.codexkkp.sdk.CodexCommandBuilder
import love.forte.tools.codexkkp.sdk.CodexExecConfig
import love.forte.tools.codexkkp.sdk.SandboxMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CodexCommandBuilderTest {

    private val testDir = "/test/project"

    // ==================== 基础命令构建测试 ====================

    @Test
    fun `build default command with cd`() {
        val config = CodexExecConfig(workingDirectory = testDir)
        val command = CodexCommandBuilder.build("分析项目", config)

        // 默认 skipGitRepoCheck=true，所以会包含 --skip-git-repo-check
        assertEquals("codex exec --json --cd $testDir --skip-git-repo-check 分析项目", command)
    }

    @Test
    fun `build always includes --json`() {
        val config = CodexExecConfig(workingDirectory = testDir)
        val command = CodexCommandBuilder.build("任务", config)

        assertTrue(command.contains("--json"))
    }

    @Test
    fun `build always includes --cd`() {
        val config = CodexExecConfig(workingDirectory = testDir)
        val command = CodexCommandBuilder.build("任务", config)

        assertTrue(command.contains("--cd $testDir"))
    }

    // ==================== --full-auto 测试 ====================

    @Test
    fun `build with fullAuto`() {
        val config = CodexExecConfig(workingDirectory = testDir, fullAuto = true)
        val command = CodexCommandBuilder.build("修复代码", config)

        assertTrue(command.contains("--full-auto"))
    }

    // ==================== --sandbox 测试 ====================

    @Test
    fun `build with sandbox danger-full-access`() {
        val config = CodexExecConfig(workingDirectory = testDir, sandbox = SandboxMode.DANGER_FULL_ACCESS)
        val command = CodexCommandBuilder.build("更新依赖", config)

        assertTrue(command.contains("--sandbox danger-full-access"))
    }

    // ==================== --output-last-message 测试 ====================

    @Test
    fun `build with outputLastMessage`() {
        val config = CodexExecConfig(workingDirectory = testDir, outputLastMessage = "./output.txt")
        val command = CodexCommandBuilder.build("生成报告", config)

        assertTrue(command.contains("--output-last-message ./output.txt"))
    }

    @Test
    fun `build with outputLastMessage containing spaces`() {
        val config = CodexExecConfig(workingDirectory = testDir, outputLastMessage = "./my output.txt")
        val command = CodexCommandBuilder.build("任务", config)

        assertTrue(command.contains("--output-last-message \"./my output.txt\""))
    }

    // ==================== --output-schema 测试 ====================

    @Test
    fun `build with outputSchema`() {
        val config = CodexExecConfig(workingDirectory = testDir, outputSchema = "./schema.json")
        val command = CodexCommandBuilder.build("提取元数据", config)

        assertTrue(command.contains("--output-schema ./schema.json"))
    }

    // ==================== --skip-git-repo-check 测试 ====================

    @Test
    fun `build with skipGitRepoCheck true outputs flag`() {
        val config = CodexExecConfig(workingDirectory = testDir, skipGitRepoCheck = true)
        val command = CodexCommandBuilder.build("执行任务", config)

        assertTrue(command.contains("--skip-git-repo-check"))
    }

    @Test
    fun `build with skipGitRepoCheck false omits flag`() {
        val config = CodexExecConfig(workingDirectory = testDir, skipGitRepoCheck = false)
        val command = CodexCommandBuilder.build("执行任务", config)

        assertFalse(command.contains("--skip-git-repo-check"))
    }

    @Test
    fun `build default skipGitRepoCheck outputs flag`() {
        // 默认值为 true，所以应该包含 --skip-git-repo-check
        val config = CodexExecConfig(workingDirectory = testDir)
        val command = CodexCommandBuilder.build("执行任务", config)

        assertTrue(command.contains("--skip-git-repo-check"))
    }

    // ==================== --session 测试 ====================

    @Test
    fun `build with session`() {
        val config = CodexExecConfig(workingDirectory = testDir, session = "abc-123")
        val command = CodexCommandBuilder.build("继续任务", config)

        assertTrue(command.contains("resume abc-123"))
    }

    @Test
    fun `build with session and fullAuto`() {
        val config = CodexExecConfig(
            workingDirectory = testDir,
            session = "abc-123",
            fullAuto = true
        )
        val command = CodexCommandBuilder.build("继续修复", config)

        assertTrue(command.contains("resume abc-123"))
        assertTrue(command.contains("--full-auto"))
    }

    // ==================== --image 测试 ====================

    @Test
    fun `build with single image`() {
        val config = CodexExecConfig(
            workingDirectory = testDir,
            images = listOf("./screenshot.png")
        )
        val command = CodexCommandBuilder.build("分析图片", config)

        assertTrue(command.contains("--image ./screenshot.png"))
    }

    @Test
    fun `build with multiple images`() {
        val config = CodexExecConfig(
            workingDirectory = testDir,
            images = listOf("./img1.png", "./img2.jpg", "./img3.gif")
        )
        val command = CodexCommandBuilder.build("分析多张图片", config)

        assertTrue(command.contains("--image ./img1.png"))
        assertTrue(command.contains("--image ./img2.jpg"))
        assertTrue(command.contains("--image ./img3.gif"))
    }

    @Test
    fun `build with image containing spaces`() {
        val config = CodexExecConfig(
            workingDirectory = testDir,
            images = listOf("./my screenshot.png")
        )
        val command = CodexCommandBuilder.build("任务", config)

        assertTrue(command.contains("--image \"./my screenshot.png\""))
    }

    @Test
    fun `build with no images`() {
        val config = CodexExecConfig(workingDirectory = testDir)
        val command = CodexCommandBuilder.build("任务", config)

        assertFalse(command.contains("--image"))
    }

    // ==================== 组合选项测试 ====================

    @Test
    fun `build with all options`() {
        val config = CodexExecConfig(
            workingDirectory = testDir,
            fullAuto = true,
            sandbox = SandboxMode.DANGER_FULL_ACCESS,
            outputLastMessage = "./output.txt",
            outputSchema = "./schema.json",
            skipGitRepoCheck = true,
            session = "session-123",
            images = listOf("./img1.png", "./img2.png")
        )
        val command = CodexCommandBuilder.build("完整任务", config)

        assertTrue(command.startsWith("codex exec --json"))
        assertTrue(command.contains("--cd $testDir"))
        assertTrue(command.contains("--full-auto"))
        assertTrue(command.contains("--sandbox danger-full-access"))
        assertTrue(command.contains("--output-last-message ./output.txt"))
        assertTrue(command.contains("--output-schema ./schema.json"))
        assertTrue(command.contains("--skip-git-repo-check"))
        assertTrue(command.contains("--image ./img1.png"))
        assertTrue(command.contains("--image ./img2.png"))
        assertTrue(command.contains("resume session-123"))
        assertTrue(command.endsWith("完整任务"))
    }

    // ==================== 参数转义测试 ====================

    @Test
    fun `build escapes task with spaces`() {
        val config = CodexExecConfig(workingDirectory = testDir)
        val command = CodexCommandBuilder.build("fix all bugs", config)

        assertTrue(command.endsWith("\"fix all bugs\""))
    }

    @Test
    fun `build escapes task with quotes`() {
        val config = CodexExecConfig(workingDirectory = testDir)
        val command = CodexCommandBuilder.build("fix \"important\" bugs", config)

        assertTrue(command.contains("\\\"important\\\""))
    }

    @Test
    fun `build simple task without escaping`() {
        val config = CodexExecConfig(workingDirectory = testDir)
        val command = CodexCommandBuilder.build("简单任务", config)

        // 不包含空格的任务不需要引号
        assertTrue(command.endsWith(" 简单任务"))
        assertFalse(command.endsWith("\"简单任务\""))
    }

    @Test
    fun `build escapes cd with spaces`() {
        val config = CodexExecConfig(workingDirectory = "/path/with spaces/project")
        val command = CodexCommandBuilder.build("任务", config)

        assertTrue(command.contains("--cd \"/path/with spaces/project\""))
    }

    // ==================== 端到端引号处理测试 ====================
    // 注意: escapeArg 函数的单元测试在 codex-kkp-sdk 模块中

    @Test
    fun `build with path containing quotes`() {
        val config = CodexExecConfig(workingDirectory = "/path/to/\"quoted\" dir")
        val command = CodexCommandBuilder.build("任务", config)

        // 验证路径被正确转义
        assertTrue(command.contains("--cd \"/path/to/\\\"quoted\\\" dir\""))
    }

    @Test
    fun `build with path containing backslash`() {
        val config = CodexExecConfig(workingDirectory = "C:\\Users\\project")
        val command = CodexCommandBuilder.build("任务", config)

        // 验证反斜杠被正确转义
        assertTrue(command.contains("--cd \"C:\\\\Users\\\\project\""))
    }

    @Test
    fun `build with task containing special shell chars`() {
        val config = CodexExecConfig(workingDirectory = testDir)
        val command = CodexCommandBuilder.build("fix \$HOME/bug", config)

        // 验证 $ 符号被正确处理（用引号包裹）
        assertTrue(command.endsWith("\"fix \$HOME/bug\""))
    }

    @Test
    fun `build with outputLastMessage containing quotes`() {
        val config = CodexExecConfig(
            workingDirectory = testDir,
            outputLastMessage = "./\"output\".txt"
        )
        val command = CodexCommandBuilder.build("任务", config)

        assertTrue(command.contains("--output-last-message \"./\\\"output\\\".txt\""))
    }
}
