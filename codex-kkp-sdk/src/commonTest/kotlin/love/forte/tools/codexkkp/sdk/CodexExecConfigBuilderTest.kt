package love.forte.tools.codexkkp.sdk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CodexExecConfigBuilderTest {

    private val testDir = "/test/project"

    // ==================== 必填参数测试 ====================

    @Test
    fun `build without workingDirectory throws exception`() {
        assertFailsWith<IllegalStateException> {
            codexConfig { }
        }
    }

    @Test
    fun `workingDirectory sets path`() {
        val config = codexConfig {
            workingDirectory(testDir)
        }

        assertEquals(testDir, config.workingDirectory)
    }

    // ==================== fullAuto 测试 ====================

    @Test
    fun `fullAuto sets flag`() {
        val config = codexConfig {
            workingDirectory(testDir)
            fullAuto()
        }

        assertTrue(config.fullAuto)
    }

    // ==================== sandbox 测试 ====================

    @Test
    fun `sandbox sets mode`() {
        val config = codexConfig {
            workingDirectory(testDir)
            sandbox(SandboxMode.DANGER_FULL_ACCESS)
        }

        assertEquals(SandboxMode.DANGER_FULL_ACCESS, config.sandbox)
    }

    @Test
    fun `dangerFullAccess sets sandbox mode`() {
        val config = codexConfig {
            workingDirectory(testDir)
            dangerFullAccess()
        }

        assertEquals(SandboxMode.DANGER_FULL_ACCESS, config.sandbox)
    }

    // ==================== outputLastMessage 测试 ====================

    @Test
    fun `outputLastMessage sets path`() {
        val config = codexConfig {
            workingDirectory(testDir)
            outputLastMessage("./output.txt")
        }

        assertEquals("./output.txt", config.outputLastMessage)
    }

    // ==================== outputSchema 测试 ====================

    @Test
    fun `outputSchema sets path`() {
        val config = codexConfig {
            workingDirectory(testDir)
            outputSchema("./schema.json")
        }

        assertEquals("./schema.json", config.outputSchema)
    }

    // ==================== skipGitRepoCheck 测试 ====================

    @Test
    fun `skipGitRepoCheck sets true`() {
        val config = codexConfig {
            workingDirectory(testDir)
            skipGitRepoCheck()
        }

        assertTrue(config.skipGitRepoCheck)
    }

    @Test
    fun `skipGitRepoCheck with true sets true`() {
        val config = codexConfig {
            workingDirectory(testDir)
            skipGitRepoCheck(true)
        }

        assertTrue(config.skipGitRepoCheck)
    }

    @Test
    fun `skipGitRepoCheck with false sets false`() {
        val config = codexConfig {
            workingDirectory(testDir)
            skipGitRepoCheck(false)
        }

        assertFalse(config.skipGitRepoCheck)
    }

    @Test
    fun `doGitRepoCheck sets false`() {
        val config = codexConfig {
            workingDirectory(testDir)
            doGitRepoCheck()
        }

        assertFalse(config.skipGitRepoCheck)
    }

    @Test
    fun `default skipGitRepoCheck is true`() {
        val config = codexConfig {
            workingDirectory(testDir)
        }

        // 默认值已改为 true
        assertTrue(config.skipGitRepoCheck)
    }

    // ==================== session 测试 ====================

    @Test
    fun `session sets id`() {
        val config = codexConfig {
            workingDirectory(testDir)
            session("abc-123")
        }

        assertEquals("abc-123", config.session)
    }

    // ==================== image 测试 ====================

    @Test
    fun `single image adds to list`() {
        val config = codexConfig {
            workingDirectory(testDir)
            image("./screenshot.png")
        }

        assertEquals(listOf("./screenshot.png"), config.images)
    }

    @Test
    fun `multiple images add to list`() {
        val config = codexConfig {
            workingDirectory(testDir)
            image("./img1.png")
            image("./img2.jpg")
            image("./img3.gif")
        }

        assertEquals(
            listOf("./img1.png", "./img2.jpg", "./img3.gif"),
            config.images
        )
    }

    @Test
    fun `no image results in empty list`() {
        val config = codexConfig {
            workingDirectory(testDir)
        }

        assertEquals(emptyList(), config.images)
    }

    // ==================== 组合测试 ====================

    @Test
    fun `multiple options combined`() {
        val config = codexConfig {
            workingDirectory(testDir)
            fullAuto()
            dangerFullAccess()
            skipGitRepoCheck()
        }

        assertEquals(testDir, config.workingDirectory)
        assertTrue(config.fullAuto)
        assertEquals(SandboxMode.DANGER_FULL_ACCESS, config.sandbox)
        assertTrue(config.skipGitRepoCheck)
    }

    @Test
    fun `all options combined`() {
        val config = codexConfig {
            workingDirectory(testDir)
            fullAuto()
            sandbox(SandboxMode.DANGER_FULL_ACCESS)
            outputLastMessage("./output.txt")
            outputSchema("./schema.json")
            skipGitRepoCheck()
            session("session-123")
            image("./img1.png")
            image("./img2.png")
        }

        assertEquals(testDir, config.workingDirectory)
        assertTrue(config.fullAuto)
        assertEquals(SandboxMode.DANGER_FULL_ACCESS, config.sandbox)
        assertEquals("./output.txt", config.outputLastMessage)
        assertEquals("./schema.json", config.outputSchema)
        assertTrue(config.skipGitRepoCheck)
        assertEquals("session-123", config.session)
        assertEquals(listOf("./img1.png", "./img2.png"), config.images)
    }

    // ==================== 覆盖测试 ====================

    @Test
    fun `later call overrides earlier`() {
        val config = codexConfig {
            workingDirectory(testDir)
            outputSchema("first.json")
            outputSchema("second.json")
        }

        assertEquals("second.json", config.outputSchema)
    }

    @Test
    fun `later session overrides earlier`() {
        val config = codexConfig {
            workingDirectory(testDir)
            session("first-id")
            session("second-id")
        }

        assertEquals("second-id", config.session)
    }

    // ==================== 等价性测试 ====================

    @Test
    fun `DSL produces same result as constructor`() {
        val dslConfig = codexConfig {
            workingDirectory(testDir)
            fullAuto()
            sandbox(SandboxMode.DANGER_FULL_ACCESS)
        }

        val constructorConfig = CodexExecConfig(
            workingDirectory = testDir,
            fullAuto = true,
            sandbox = SandboxMode.DANGER_FULL_ACCESS
        )

        assertEquals(constructorConfig, dslConfig)
    }
}
