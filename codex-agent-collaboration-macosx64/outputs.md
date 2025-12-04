# Advanced Output Options

This document describes advanced output options for the Codex CLI.

## Output Parameters

| Parameter                      | Description                                                   |
|--------------------------------|---------------------------------------------------------------|
| `--full`                       | Return all events (large output, use only when needed)        |
| `--output-last-message=<file>` | Save last message to file, **absolute path required**         |
| `--output-schema=<path>`       | Output schema JSON file, **absolute path required**           |

## Full Mode (`--full`)

When `--full` is specified, the CLI returns all events instead of the summarized response. 
This produces significantly larger output and should only be used when you need detailed event-level information.

### Full Mode Response Format

```json
{
  "type": "SUCCESS",
  "session": "thread_abc123xyz",
  "content": {
    "fullEvents": [
      {"type": "thread.started", "threadId": "thread_abc123xyz"},
      {"type": "turn.started", "turnId": "turn_001"},
      {"type": "item.completed",
        "item": {
          "id": "item_001",
          "type": "agent_message",
          "text": "..."
        }},
      {
        "type": "item.completed",
        "item": {
          "id": "item_002",
          "type": "file_change",
          "changes": [ ... ],
          "status": "completed"
        }
      },
      {"type": "turn.completed", "turnId": "turn_001"}
    ]
  }
}
```

### Item Types (within `item` events)

| Item Type           | Description                          |
|---------------------|--------------------------------------|
| `agent_message`     | Text response from Codex             |
| `file_change`       | File modifications                   |
| `command_execution` | Shell command execution              |
| `reasoning`         | Internal reasoning process           |
| `mcp_tool_call`     | MCP tool invocation                  |
| `web_search`        | Web search query                     |
| `todo_list`         | Task tracking list                   |
| `error`             | Non-fatal error                      |

## Output to File (`--output-last-message`)

Save the last agent message to a specified file:

```bash
executable/codex-kkp-cli \
  --cd="/path/to/project" \
  --output-last-message="/path/to/output/response.txt" \
  "Analyze this code"
```

## Output Schema (`--output-schema`)

Specify a JSON schema file for structured output:

```bash
executable/codex-kkp-cli \
  --cd="/path/to/project" \
  --output-schema="/path/to/schema.json" \
  "Extract function signatures"
```
