package love.forte.tools.codexkkp.sdk.model

import kotlin.test.*

class CodexParserTest {

    // ==================== 单行事件解析测试 ====================

    @Test
    fun `parseEvent thread started`() {
        val json = """{"type":"thread.started","thread_id":"0199a213-81c0-7800-8aa1-bbab2a035a53"}"""
        val event = CodexParser.parseEvent(json)

        assertTrue(event is ThreadStartedEvent)
        assertEquals("0199a213-81c0-7800-8aa1-bbab2a035a53", event.threadId)
    }

    @Test
    fun `parseEvent turn started`() {
        val json = """{"type":"turn.started"}"""
        val event = CodexParser.parseEvent(json)

        assertTrue(event is TurnStartedEvent)
    }

    @Test
    fun `parseEvent turn completed`() {
        val json =
            """{"type":"turn.completed","usage":{"input_tokens":24763,"cached_input_tokens":24448,"output_tokens":122}}"""
        val event = CodexParser.parseEvent(json)

        assertTrue(event is TurnCompletedEvent)
        assertEquals(24763, event.usage.inputTokens)
        assertEquals(24448, event.usage.cachedInputTokens)
        assertEquals(122, event.usage.outputTokens)
    }

    @Test
    fun `parseEvent turn failed`() {
        val json = """{"type":"turn.failed"}"""
        val event = CodexParser.parseEvent(json)

        assertTrue(event is TurnFailedEvent)
        assertNull(event.error)
    }

    @Test
    fun `parseEvent turn failed with error`() {
        val json = """{"type":"turn.failed","error":{"message":"Connection timeout"}}"""
        val event = CodexParser.parseEvent(json)

        assertTrue(event is TurnFailedEvent)
        assertNotNull(event.error)
        assertEquals("Connection timeout", event.error!!.message)
    }

    @Test
    fun `parseEvent error`() {
        val json = """{"type":"error","message":"Fatal error occurred"}"""
        val event = CodexParser.parseEvent(json)

        assertTrue(event is ThreadErrorEvent)
        assertEquals("Fatal error occurred", event.message)
    }

    // ==================== Item 事件解析测试 ====================

    @Test
    fun `parseEvent item started with command_execution`() {
        val json =
            """{"type":"item.started","item":{"id":"item_1","type":"command_execution","command":"bash -lc ls","status":"in_progress"}}"""
        val event = CodexParser.parseEvent(json)

        assertTrue(event is ItemStartedEvent)
        val item = event.item
        assertTrue(item is CommandExecutionItem)
        assertEquals("item_1", item.id)
        assertEquals("bash -lc ls", item.command)
        assertEquals(CommandExecutionStatus.IN_PROGRESS, item.status)
    }

    @Test
    fun `parseEvent item started with command_execution full fields`() {
        val json =
            """{"type":"item.started","item":{"id":"item_1","type":"command_execution","command":"ls -la","aggregated_output":"file1\nfile2","exit_code":0,"status":"completed"}}"""
        val event = CodexParser.parseEvent(json)

        assertTrue(event is ItemStartedEvent)
        val item = event.item
        assertTrue(item is CommandExecutionItem)
        assertEquals("item_1", item.id)
        assertEquals("ls -la", item.command)
        assertEquals("file1\nfile2", item.aggregatedOutput)
        assertEquals(0, item.exitCode)
        assertEquals(CommandExecutionStatus.COMPLETED, item.status)
    }

    @Test
    fun `parseEvent item completed with agent_message`() {
        val json =
            """{"type":"item.completed","item":{"id":"item_3","type":"agent_message","text":"Repo contains docs, sdk, and examples directories."}}"""
        val event = CodexParser.parseEvent(json)

        assertTrue(event is ItemCompletedEvent)
        val item = event.item
        assertTrue(item is AgentMessageItem)
        assertEquals("item_3", item.id)
        assertEquals("Repo contains docs, sdk, and examples directories.", item.text)
    }

    @Test
    fun `parseEvent item with reasoning`() {
        val json = """{"type":"item.completed","item":{"id":"item_2","type":"reasoning","text":"分析项目结构..."}}"""
        val event = CodexParser.parseEvent(json)

        assertTrue(event is ItemCompletedEvent)
        val item = event.item
        assertTrue(item is ReasoningItem)
        assertEquals("分析项目结构...", item.text)
    }

    @Test
    fun `parseEvent item updated`() {
        val json =
            """{"type":"item.updated","item":{"id":"item_1","type":"command_execution","command":"npm install","aggregated_output":"Installing...","status":"in_progress"}}"""
        val event = CodexParser.parseEvent(json)

        assertTrue(event is ItemUpdatedEvent)
        val item = event.item
        assertTrue(item is CommandExecutionItem)
        assertEquals("item_1", item.id)
        assertEquals("Installing...", item.aggregatedOutput)
    }

    // ==================== 新 Item 类型解析测试 ====================

    @Test
    fun `parseEvent item with file_change`() {
        val json =
            """{"type":"item.completed","item":{"id":"item_fc","type":"file_change","changes":[{"path":"src/main.kt","kind":"update"},{"path":"src/new.kt","kind":"add"}],"status":"completed"}}"""
        val event = CodexParser.parseEvent(json)

        assertTrue(event is ItemCompletedEvent)
        val item = event.item
        assertTrue(item is FileChangeItem)
        assertEquals("item_fc", item.id)
        assertEquals(2, item.changes.size)
        assertEquals("src/main.kt", item.changes[0].path)
        assertEquals(PatchChangeKind.UPDATE, item.changes[0].kind)
        assertEquals("src/new.kt", item.changes[1].path)
        assertEquals(PatchChangeKind.ADD, item.changes[1].kind)
        assertEquals(PatchApplyStatus.COMPLETED, item.status)
    }

    @Test
    fun `parseEvent item with mcp_tool_call`() {
        val json =
            """{"type":"item.completed","item":{"id":"item_mcp","type":"mcp_tool_call","server":"file-server","tool":"read_file","status":"completed"}}"""
        val event = CodexParser.parseEvent(json)

        assertTrue(event is ItemCompletedEvent)
        val item = event.item
        assertTrue(item is McpToolCallItem)
        assertEquals("item_mcp", item.id)
        assertEquals("file-server", item.server)
        assertEquals("read_file", item.tool)
        assertEquals(McpToolCallStatus.COMPLETED, item.status)
    }

    @Test
    fun `parseEvent item with web_search`() {
        val json =
            """{"type":"item.completed","item":{"id":"item_ws","type":"web_search","query":"kotlin multiplatform tutorial"}}"""
        val event = CodexParser.parseEvent(json)

        assertTrue(event is ItemCompletedEvent)
        val item = event.item
        assertTrue(item is WebSearchItem)
        assertEquals("item_ws", item.id)
        assertEquals("kotlin multiplatform tutorial", item.query)
    }

    @Test
    fun `parseEvent item with error`() {
        val json =
            """{"type":"item.completed","item":{"id":"item_err","type":"error","message":"File not found"}}"""
        val event = CodexParser.parseEvent(json)

        assertTrue(event is ItemCompletedEvent)
        val item = event.item
        assertTrue(item is ErrorItem)
        assertEquals("item_err", item.id)
        assertEquals("File not found", item.message)
    }

    @Test
    fun `parseEvent item with todo_list`() {
        val json =
            """{"type":"item.completed","item":{"id":"item_todo","type":"todo_list","items":[{"text":"Fix bug","completed":true},{"text":"Write tests","completed":false}]}}"""
        val event = CodexParser.parseEvent(json)

        assertTrue(event is ItemCompletedEvent)
        val item = event.item
        assertTrue(item is TodoListItem)
        assertEquals("item_todo", item.id)
        assertEquals(2, item.items.size)
        assertEquals("Fix bug", item.items[0].text)
        assertTrue(item.items[0].completed)
        assertEquals("Write tests", item.items[1].text)
        assertFalse(item.items[1].completed)
    }

    // ==================== 事件流解析测试 ====================

    @Test
    fun `parseEventStream parses multiple lines`() {
        val jsonLines = """
            {"type":"thread.started","thread_id":"test-id"}
            {"type":"turn.started"}
            {"type":"item.completed","item":{"id":"item_1","type":"agent_message","text":"Hello"}}
            {"type":"turn.completed","usage":{"input_tokens":100,"output_tokens":50}}
        """.trimIndent()

        val events = CodexParser.parseEventStream(jsonLines)

        assertEquals(4, events.size)
        assertTrue(events[0] is ThreadStartedEvent)
        assertTrue(events[1] is TurnStartedEvent)
        assertTrue(events[2] is ItemCompletedEvent)
        assertTrue(events[3] is TurnCompletedEvent)
    }

    @Test
    fun `parseEventStream ignores blank lines`() {
        val jsonLines = """
            {"type":"turn.started"}

            {"type":"turn.completed","usage":{"input_tokens":100,"output_tokens":50}}

        """.trimIndent()

        val events = CodexParser.parseEventStream(jsonLines)

        assertEquals(2, events.size)
    }

    @Test
    fun `parseEventStream ignores invalid json`() {
        val jsonLines = """
            {"type":"turn.started"}
            invalid json here
            {"type":"turn.completed","usage":{"input_tokens":100,"output_tokens":50}}
        """.trimIndent()

        val events = CodexParser.parseEventStream(jsonLines)

        assertEquals(2, events.size)
    }

    // ==================== 扩展函数测试 ====================

    @Test
    fun `getAgentMessages extracts messages`() {
        val events = listOf(
            TurnStartedEvent,
            ItemCompletedEvent(AgentMessageItem("1", "第一条消息")),
            ItemCompletedEvent(CommandExecutionItem("2", "ls", status = CommandExecutionStatus.COMPLETED)),
            ItemCompletedEvent(AgentMessageItem("3", "第二条消息")),
            TurnCompletedEvent(Usage(100, 0, 50))
        )

        val messages = events.getAgentMessages()

        assertEquals(2, messages.size)
        assertEquals("第一条消息", messages[0])
        assertEquals("第二条消息", messages[1])
    }

    @Test
    fun `getAgentMessages returns empty for no messages`() {
        val events = listOf(
            TurnStartedEvent,
            ItemCompletedEvent(CommandExecutionItem("1", "ls", status = CommandExecutionStatus.COMPLETED)),
            TurnCompletedEvent(Usage(100, 0, 50))
        )

        val messages = events.getAgentMessages()

        assertTrue(messages.isEmpty())
    }

    @Test
    fun `getUsage extracts usage from turn completed`() {
        val events = listOf(
            TurnStartedEvent,
            TurnCompletedEvent(Usage(1000, 500, 200))
        )

        val usage = events.getUsage()

        assertNotNull(usage)
        assertEquals(1000, usage.inputTokens)
        assertEquals(500, usage.cachedInputTokens)
        assertEquals(200, usage.outputTokens)
    }

    @Test
    fun `getUsage returns null if no turn completed`() {
        val events = listOf(
            TurnStartedEvent,
            ItemCompletedEvent(AgentMessageItem("1", "消息"))
        )

        val usage = events.getUsage()

        assertNull(usage)
    }

    @Test
    fun `filterByType filters correctly`() {
        val events = listOf(
            ThreadStartedEvent("id-1"),
            TurnStartedEvent,
            ItemCompletedEvent(AgentMessageItem("1", "消息")),
            TurnCompletedEvent(Usage(100, 0, 50))
        )

        val itemEvents = events.filterByType<ItemCompletedEvent>()

        assertEquals(1, itemEvents.size)
        assertTrue(itemEvents[0].item is AgentMessageItem)
    }

    // ==================== 容错性测试 ====================

    @Test
    fun `parseEvent returns null for invalid json`() {
        val event = CodexParser.parseEvent("not valid json")

        assertNull(event)
    }

    @Test
    fun `parseEvent handles unknown fields gracefully`() {
        val json = """{"type":"turn.started","unknown_field":"value"}"""
        val event = CodexParser.parseEvent(json)

        // ignoreUnknownKeys 应该允许这个通过
        assertTrue(event is TurnStartedEvent)
    }

    @Test
    fun `parseEvent usage without cached tokens`() {
        val json = """{"type":"turn.completed","usage":{"input_tokens":100,"output_tokens":50}}"""
        val event = CodexParser.parseEvent(json)

        assertTrue(event is TurnCompletedEvent)
        val usage = event.usage
        assertEquals(100, usage.inputTokens)
        assertEquals(0, usage.cachedInputTokens) // defaults to 0
        assertEquals(50, usage.outputTokens)
    }

    @Test
    fun `parseEvent command execution with all statuses`() {
        val statuses = listOf("in_progress", "completed", "failed")
        val expected = listOf(
            CommandExecutionStatus.IN_PROGRESS,
            CommandExecutionStatus.COMPLETED,
            CommandExecutionStatus.FAILED
        )

        statuses.forEachIndexed { index, status ->
            val json =
                """{"type":"item.started","item":{"id":"item_$index","type":"command_execution","command":"test","status":"$status"}}"""
            val event = CodexParser.parseEvent(json)

            assertTrue(event is ItemStartedEvent)
            val item = event.item
            assertTrue(item is CommandExecutionItem)
            assertEquals(expected[index], item.status)
        }
    }
}
