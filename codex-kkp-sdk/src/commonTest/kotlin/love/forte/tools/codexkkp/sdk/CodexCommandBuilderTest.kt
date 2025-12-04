package love.forte.tools.codexkkp.sdk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CodexCommandBuilderTest {

    // ==================== escapeArg 函数测试 ====================

    @Test
    fun `escapeArg empty string returns empty quotes`() {
        assertEquals("\"\"", CodexCommandBuilder.escapeArg(""))
    }

    @Test
    fun `escapeArg simple string returns as-is`() {
        assertEquals("simple", CodexCommandBuilder.escapeArg("simple"))
        assertEquals("/path/to/file", CodexCommandBuilder.escapeArg("/path/to/file"))
    }

    @Test
    fun `escapeArg string with spaces gets quoted`() {
        assertEquals("\"path with spaces\"", CodexCommandBuilder.escapeArg("path with spaces"))
    }

    @Test
    fun `escapeArg string with quotes gets escaped and quoted`() {
        assertEquals("\"value with \\\"quotes\\\"\"", CodexCommandBuilder.escapeArg("value with \"quotes\""))
    }

    @Test
    fun `escapeArg string with backslash gets escaped and quoted`() {
        assertEquals("\"path\\\\to\\\\file\"", CodexCommandBuilder.escapeArg("path\\to\\file"))
    }

    @Test
    fun `escapeArg string with both quotes and backslash`() {
        // C:\Users\"John" -> "C:\\Users\\\"John\""
        assertEquals(
            "\"C:\\\\Users\\\\\\\"John\\\"\"",
            CodexCommandBuilder.escapeArg("C:\\Users\\\"John\"")
        )
    }

    @Test
    fun `escapeArg string with shell special chars gets quoted`() {
        // 测试各种 shell 特殊字符
        assertTrue(CodexCommandBuilder.escapeArg("value\$var").startsWith("\""))
        assertTrue(CodexCommandBuilder.escapeArg("value`cmd`").startsWith("\""))
        assertTrue(CodexCommandBuilder.escapeArg("value!bang").startsWith("\""))
        assertTrue(CodexCommandBuilder.escapeArg("value*glob").startsWith("\""))
        assertTrue(CodexCommandBuilder.escapeArg("value?glob").startsWith("\""))
        assertTrue(CodexCommandBuilder.escapeArg("value[0]").startsWith("\""))
        assertTrue(CodexCommandBuilder.escapeArg("value{a,b}").startsWith("\""))
        assertTrue(CodexCommandBuilder.escapeArg("value(sub)").startsWith("\""))
        assertTrue(CodexCommandBuilder.escapeArg("value<redirect").startsWith("\""))
        assertTrue(CodexCommandBuilder.escapeArg("value>redirect").startsWith("\""))
        assertTrue(CodexCommandBuilder.escapeArg("value|pipe").startsWith("\""))
        assertTrue(CodexCommandBuilder.escapeArg("value&background").startsWith("\""))
        assertTrue(CodexCommandBuilder.escapeArg("value;semicolon").startsWith("\""))
        assertTrue(CodexCommandBuilder.escapeArg("value'quote").startsWith("\""))
    }

    @Test
    fun `escapeArg string with newline gets quoted`() {
        assertTrue(CodexCommandBuilder.escapeArg("line1\nline2").startsWith("\""))
        assertTrue(CodexCommandBuilder.escapeArg("line1\tline2").startsWith("\""))
    }
}
