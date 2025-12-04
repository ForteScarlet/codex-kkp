package love.forte.tools.codexkkp.cli

import love.forte.tools.codexkkp.sdk.SandboxMode
import kotlin.test.*

class CliParserTest {

    private val testDir = "/test/project"

    // ==================== 基础解析测试 ====================

    @Test
    fun `parse simple task with cd`() {
        val args = arrayOf("--cd=$testDir", "分析项目")
        val result = CliParser.parse(args)

        assertEquals("分析项目", result.task)
        assertEquals(testDir, result.config.workingDirectory)
    }

    @Test
    fun `parse cd only without task`() {
        val args = arrayOf("--cd=$testDir")
        val result = CliParser.parse(args)

        assertNull(result.task)
        assertEquals(testDir, result.config.workingDirectory)
    }

    @Test
    fun `parse missing cd throws exception`() {
        val args = arrayOf("任务")

        val exception = assertFailsWith<IllegalArgumentException> {
            CliParser.parse(args)
        }
        assertTrue(exception.message!!.contains("--cd"))
    }

    @Test
    fun `parse empty args throws exception`() {
        val args = emptyArray<String>()

        assertFailsWith<IllegalArgumentException> {
            CliParser.parse(args)
        }
    }

    // ==================== --full-auto 测试 ====================

    @Test
    fun `parse --full-auto flag`() {
        val args = arrayOf("--cd=$testDir", "--full-auto", "修复 TODO")
        val result = CliParser.parse(args)

        assertEquals("修复 TODO", result.task)
        assertTrue(result.config.fullAuto)
    }

    @Test
    fun `parse --full-auto after task`() {
        val args = arrayOf("--cd=$testDir", "修复 TODO", "--full-auto")
        val result = CliParser.parse(args)

        assertEquals("修复 TODO", result.task)
        assertTrue(result.config.fullAuto)
    }

    // ==================== --sandbox 测试 ====================

    @Test
    fun `parse --sandbox=danger-full-access`() {
        val args = arrayOf("--cd=$testDir", "--sandbox=danger-full-access", "更新依赖")
        val result = CliParser.parse(args)

        assertEquals("更新依赖", result.task)
        assertEquals(SandboxMode.DANGER_FULL_ACCESS, result.config.sandbox)
    }

    @Test
    fun `parse --sandbox=read-only`() {
        val args = arrayOf("--cd=$testDir", "--sandbox=read-only", "只读任务")
        val result = CliParser.parse(args)

        assertEquals("只读任务", result.task)
        assertEquals(SandboxMode.READ_ONLY, result.config.sandbox)
    }

    @Test
    fun `parse --sandbox=workspace-write`() {
        val args = arrayOf("--cd=$testDir", "--sandbox=workspace-write", "工作区写入")
        val result = CliParser.parse(args)

        assertEquals("工作区写入", result.task)
        assertEquals(SandboxMode.WORKSPACE_WRITE, result.config.sandbox)
    }

    @Test
    fun `parse all sandbox modes via enum`() {
        // 确保所有 SandboxMode 枚举值都能被正确解析
        for (mode in SandboxMode.entries) {
            val args = arrayOf("--cd=$testDir", "--sandbox=${mode.value}", "任务")
            val result = CliParser.parse(args)
            assertEquals(mode, result.config.sandbox)
        }
    }

    @Test
    fun `parse unknown sandbox mode throws exception`() {
        val args = arrayOf("--cd=$testDir", "--sandbox=unknown", "task")

        assertFailsWith<IllegalArgumentException> {
            CliParser.parse(args)
        }
    }

    // ==================== --output-last-message 测试 ====================

    @Test
    fun `parse --output-last-message`() {
        val args = arrayOf("--cd=$testDir", "--output-last-message=./output.txt", "生成报告")
        val result = CliParser.parse(args)

        assertEquals("生成报告", result.task)
        assertEquals("./output.txt", result.config.outputLastMessage)
    }

    // ==================== --output-schema 测试 ====================

    @Test
    fun `parse --output-schema`() {
        val args = arrayOf("--cd=$testDir", "--output-schema=./schema.json", "提取元数据")
        val result = CliParser.parse(args)

        assertEquals("提取元数据", result.task)
        assertEquals("./schema.json", result.config.outputSchema)
    }

    // ==================== --skip-git-repo-check 测试 ====================

    @Test
    fun `parse --skip-git-repo-check standalone sets true`() {
        // 测试向后兼容性：独立标志仍然有效
        val args = arrayOf("--cd=$testDir", "--skip-git-repo-check", "执行任务")
        val result = CliParser.parse(args)

        assertEquals("执行任务", result.task)
        assertTrue(result.config.skipGitRepoCheck)
    }

    @Test
    fun `parse --skip-git-repo-check=true`() {
        val args = arrayOf("--cd=$testDir", "--skip-git-repo-check=true", "执行任务")
        val result = CliParser.parse(args)

        assertEquals("执行任务", result.task)
        assertTrue(result.config.skipGitRepoCheck)
    }

    @Test
    fun `parse --skip-git-repo-check=false`() {
        val args = arrayOf("--cd=$testDir", "--skip-git-repo-check=false", "执行任务")
        val result = CliParser.parse(args)

        assertEquals("执行任务", result.task)
        assertFalse(result.config.skipGitRepoCheck)
    }

    @Test
    fun `parse --skip-git-repo-check=TRUE case insensitive`() {
        val args = arrayOf("--cd=$testDir", "--skip-git-repo-check=TRUE", "执行任务")
        val result = CliParser.parse(args)

        assertTrue(result.config.skipGitRepoCheck)
    }

    @Test
    fun `parse --skip-git-repo-check=False case insensitive`() {
        val args = arrayOf("--cd=$testDir", "--skip-git-repo-check=False", "执行任务")
        val result = CliParser.parse(args)

        assertFalse(result.config.skipGitRepoCheck)
    }

    @Test
    fun `parse --skip-git-repo-check=invalid throws exception`() {
        val args = arrayOf("--cd=$testDir", "--skip-git-repo-check=yes", "task")

        val exception = assertFailsWith<IllegalArgumentException> {
            CliParser.parse(args)
        }
        assertTrue(exception.message!!.contains("Invalid boolean value"))
    }

    @Test
    fun `parse default skipGitRepoCheck is true`() {
        val args = arrayOf("--cd=$testDir", "执行任务")
        val result = CliParser.parse(args)

        // 默认值已改为 true
        assertTrue(result.config.skipGitRepoCheck)
    }

    // ==================== --session 测试 ====================

    @Test
    fun `parse --session`() {
        val args = arrayOf("--cd=$testDir", "--session=abc-123", "继续任务")
        val result = CliParser.parse(args)

        assertEquals("继续任务", result.task)
        assertEquals("abc-123", result.config.session)
    }

    @Test
    fun `parse --session with full-auto`() {
        val args = arrayOf("--cd=$testDir", "--session=abc-123", "--full-auto", "继续任务")
        val result = CliParser.parse(args)

        assertEquals("继续任务", result.task)
        assertEquals("abc-123", result.config.session)
        assertTrue(result.config.fullAuto)
    }

    // ==================== --image 测试 ====================

    @Test
    fun `parse single --image`() {
        val args = arrayOf("--cd=$testDir", "--image=./screenshot.png", "分析图片")
        val result = CliParser.parse(args)

        assertEquals("分析图片", result.task)
        assertEquals(listOf("./screenshot.png"), result.config.images)
    }

    @Test
    fun `parse multiple --image`() {
        val args = arrayOf(
            "--cd=$testDir",
            "--image=./img1.png",
            "--image=./img2.jpg",
            "--image=./img3.gif",
            "分析多张图片"
        )
        val result = CliParser.parse(args)

        assertEquals("分析多张图片", result.task)
        assertEquals(
            listOf("./img1.png", "./img2.jpg", "./img3.gif"),
            result.config.images
        )
    }

    @Test
    fun `parse no --image returns empty list`() {
        val args = arrayOf("--cd=$testDir", "无图片任务")
        val result = CliParser.parse(args)

        assertEquals(emptyList(), result.config.images)
    }

    // ==================== 组合选项测试 ====================

    @Test
    fun `parse multiple options combined`() {
        val args = arrayOf(
            "--cd=$testDir",
            "--full-auto",
            "--sandbox=danger-full-access",
            "--skip-git-repo-check",
            "执行复杂任务"
        )
        val result = CliParser.parse(args)

        assertEquals("执行复杂任务", result.task)
        assertEquals(testDir, result.config.workingDirectory)
        assertTrue(result.config.fullAuto)
        assertEquals(SandboxMode.DANGER_FULL_ACCESS, result.config.sandbox)
        assertTrue(result.config.skipGitRepoCheck)
    }

    @Test
    fun `parse all options combined`() {
        val args = arrayOf(
            "--cd=$testDir",
            "--session=session-123",
            "--full-auto",
            "--sandbox=danger-full-access",
            "--output-last-message=./output.txt",
            "--output-schema=./schema.json",
            "--skip-git-repo-check",
            "--image=./img1.png",
            "--image=./img2.png",
            "完整任务"
        )
        val result = CliParser.parse(args)

        assertEquals("完整任务", result.task)
        assertEquals(testDir, result.config.workingDirectory)
        assertEquals("session-123", result.config.session)
        assertTrue(result.config.fullAuto)
        assertEquals(SandboxMode.DANGER_FULL_ACCESS, result.config.sandbox)
        assertEquals("./output.txt", result.config.outputLastMessage)
        assertEquals("./schema.json", result.config.outputSchema)
        assertTrue(result.config.skipGitRepoCheck)
        assertEquals(listOf("./img1.png", "./img2.png"), result.config.images)
    }

    // ==================== 错误处理测试 ====================

    @Test
    fun `parse unknown option throws exception`() {
        val args = arrayOf("--cd=$testDir", "--unknown-option", "task")

        assertFailsWith<IllegalArgumentException> {
            CliParser.parse(args)
        }
    }

    @Test
    fun `parse duplicate task throws exception`() {
        val args = arrayOf("--cd=$testDir", "task1", "task2")

        assertFailsWith<IllegalArgumentException> {
            CliParser.parse(args)
        }
    }

    // ==================== 引号处理测试 ====================

    @Test
    fun `parseQuotedValue with no quotes returns as-is`() {
        assertEquals("simple", CliParser.parseQuotedValue("simple"))
        assertEquals("/path/to/file", CliParser.parseQuotedValue("/path/to/file"))
    }

    @Test
    fun `parseQuotedValue removes surrounding quotes`() {
        assertEquals("value", CliParser.parseQuotedValue("\"value\""))
        assertEquals("path with spaces", CliParser.parseQuotedValue("\"path with spaces\""))
    }

    @Test
    fun `parseQuotedValue handles escaped quotes`() {
        // "value with \"escaped\" quotes" -> value with "escaped" quotes
        assertEquals(
            "value with \"escaped\" quotes",
            CliParser.parseQuotedValue("\"value with \\\"escaped\\\" quotes\"")
        )
    }

    @Test
    fun `parseQuotedValue handles escaped backslash`() {
        // "path\\to\\file" -> path\to\file
        assertEquals(
            "path\\to\\file",
            CliParser.parseQuotedValue("\"path\\\\to\\\\file\"")
        )
    }

    @Test
    fun `parseQuotedValue handles mixed escapes`() {
        // "C:\\Users\\\"John\"" -> C:\Users\"John"
        assertEquals(
            "C:\\Users\\\"John\"",
            CliParser.parseQuotedValue("\"C:\\\\Users\\\\\\\"John\\\"\"")
        )
    }

    @Test
    fun `parseQuotedValue preserves unknown escape sequences`() {
        // "value with \n newline" -> 保留 \n
        assertEquals(
            "value with \\n newline",
            CliParser.parseQuotedValue("\"value with \\n newline\"")
        )
    }

    @Test
    fun `parseQuotedValue empty quoted string`() {
        assertEquals("", CliParser.parseQuotedValue("\"\""))
    }

    @Test
    fun `parseQuotedValue single quote not treated as quoted`() {
        // 只有一个引号不算引号包裹
        assertEquals("\"", CliParser.parseQuotedValue("\""))
        assertEquals("\"value", CliParser.parseQuotedValue("\"value"))
        assertEquals("value\"", CliParser.parseQuotedValue("value\""))
    }

    @Test
    fun `parse --cd with quoted path containing spaces`() {
        val args = arrayOf("--cd=\"/path/with spaces/project\"", "任务")
        val result = CliParser.parse(args)

        assertEquals("/path/with spaces/project", result.config.workingDirectory)
    }

    @Test
    fun `parse --cd with quoted path containing escaped quotes`() {
        val args = arrayOf("--cd=\"/path/to/\\\"quoted\\\" dir\"", "任务")
        val result = CliParser.parse(args)

        assertEquals("/path/to/\"quoted\" dir", result.config.workingDirectory)
    }

    @Test
    fun `parse --image with quoted path containing spaces`() {
        val args = arrayOf(
            "--cd=$testDir",
            "--image=\"/path/with spaces/image.png\"",
            "任务"
        )
        val result = CliParser.parse(args)

        assertEquals(listOf("/path/with spaces/image.png"), result.config.images)
    }

    @Test
    fun `parse --output-last-message with quoted path`() {
        val args = arrayOf(
            "--cd=$testDir",
            "--output-last-message=\"./my output file.txt\"",
            "任务"
        )
        val result = CliParser.parse(args)

        assertEquals("./my output file.txt", result.config.outputLastMessage)
    }

    @Test
    fun `parse --session with quoted value containing special chars`() {
        val args = arrayOf(
            "--cd=$testDir",
            "--session=\"session-with-\\\"quotes\\\"\"",
            "任务"
        )
        val result = CliParser.parse(args)

        assertEquals("session-with-\"quotes\"", result.config.session)
    }
}
