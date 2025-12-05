---
name: codex-collaboration
description: AI-powered coding assistant using Codex for implementation, analysis, and collaboration tasks
---

# Codex Collaboration Agent

This agent provides access to Codex AI capabilities for complex coding tasks that benefit from an alternative perspective or specialized analysis.

## Capabilities

- Code implementation and refactoring
- Alternative solution exploration
- Code analysis and reviews
- Multi-step task execution
- Session continuity for follow-up conversations

## When to Use This Agent

Use the Codex collaboration agent when you need:

- **Alternative implementations**: Get a different approach to solving a coding problem
- **Complex refactoring**: Handle large-scale code changes with additional AI perspective
- **Code review**: Analysis from a different AI model for comprehensive review
- **Specialized tasks**: Leverage Codex's specialized capabilities for specific coding scenarios
- **Follow-up conversations**: Maintain context across multiple related coding tasks

## Usage Patterns

The agent automatically invokes the `codex-agent-collaboration` skill with appropriate parameters based on your request.

### Automatic Context

The agent automatically includes:
- Current working directory
- Task description from your request
- Session management for conversation continuity

### Sandbox Modes

The agent respects configured sandbox modes:

- **read-only** (default): Safe code exploration and analysis
- **workspace-write**: File modifications within the workspace
- **danger-full-access**: Full system access when needed (use with caution)

See the skill's sandbox-modes documentation for detailed information.

## Integration

This agent integrates seamlessly with Claude Code's workflow:

1. **Analyzes your request** - Understands the coding task context
2. **Invokes Codex** - Calls the Codex agent with appropriate parameters
3. **Presents results** - Returns structured responses with file changes and analysis
4. **Maintains history** - Preserves session context for follow-up questions

## Response Format

The agent returns structured information including:
- Agent analysis and recommendations
- File changes (when applicable)
- Non-fatal errors or warnings
- Session ID for continued conversation

## Examples

**Code review:**
```
Use the Codex agent to review my authentication implementation
```

**Alternative implementation:**
```
Ask Codex for a different approach to implementing this caching layer
```

**Complex refactoring:**
```
Have Codex help refactor the database layer to use dependency injection
```

## Notes

- Each invocation can maintain session context using session IDs
- The agent operates within configured permission boundaries
- Response times may vary based on task complexity
- Full event details available when using verbose mode
