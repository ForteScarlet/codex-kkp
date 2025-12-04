---
name: codex-agent-collaboration
description: Execute tasks using Codex AI agent for code analysis, implementation, and collaboration
---

# Codex CLI Skill

This skill enables Claude Code to execute tasks using OpenAI's Codex AI agent.

## Overview

The `codex-kkp-cli` is a Codex Agent CLI tool, allowing you to:

- Execute coding tasks and get implementations
- Perform code analysis and reviews
- Get alternative solutions and suggestions
- Collaborate with Codex for cross-checking implementations

## Usage

```bash
executable/codex-kkp-cli --cd=/absolute/path/to/project [options] "<task_description>"
```

### Required Parameters

| Parameter    | Description                                                |
|--------------|------------------------------------------------------------|
| Task         | The task description (positional argument, must be quoted) |
| `--cd=<dir>` | Working directory (ABSOLUTE PATH REQUIRED)                 |

### Optional Parameters

| Parameter                      | Description                                                                    |
|--------------------------------|--------------------------------------------------------------------------------|
| `--session=<id>`               | Session ID (STRONGLY RECOMMENDED for follow-up chats to maintain context)      |
| `--sandbox=<mode>`             | Sandbox mode. Default is `read-only`. See [sandbox-modes.md](sandbox-modes.md) |
| `--full-auto`                  | Allow Codex to edit files automatically                                        |
| `--image=<path>`               | Include an image file (ABSOLUTE PATH, can repeat)                              |
| `--skip-git-repo-check[=BOOL]` | Skip Git repository check. Default is `true`. Use `=false` to enable Git check |

For output options (`--full`, `--output-last-message`, `--output-schema`), see [outputs.md](outputs.md).

NOTE that parameters and values are connected by an EQUAL SIGN `=`, not a space.

## Response Format

Returns JSON with `"type": "SUCCESS"` or `"type": "ERROR"`.

```JSON
{
  "type": "SUCCESS",
  "session": "xxxxxxx",
  "content": {
    "agentMessages": "I've analyzed the code and found...",
    "fileChanges": [
      ...
    ],
    "nonFatalErrors": [
      ...
    ]
  }
}
```

- `fileChanges` and `nonFatalErrors` is nullable.
- Error responses do NOT include a `session` field.

## Quick Example

New Session:

```bash
executable/codex-kkp-cli --cd=/path/to/project "Explain the main function in Main.kt"
```

Continue Previous Session:

```bash
executable/codex-kkp-cli --cd=/path/to/project --session=xxxxxxx "Explain the main function in Main.kt"
```

More examples: [examples.md](examples.md)
