# Sandbox Modes

This document describes the sandbox modes available in the Codex CLI.

## Overview

Sandbox modes control what Codex can access and modify during task execution. 
Choose the appropriate mode based on your task requirements.

## Available Modes

### `read-only` (Default)

Codex can only read files in the workspace. No modifications are allowed.

```bash
executables/codex-kkp-cli-{platform} \
  --cd="/path/to/project" \
  --sandbox=read-only \
  "Analyze this codebase"
```

**Use for:**
- Code review and analysis
- Documentation generation
- Understanding codebase structure
- Getting explanations

### `workspace-write`

Codex can read and write files within the workspace directory.

```bash
executables/codex-kkp-cli-{platform} \
  --cd="/path/to/project" \
  --sandbox=workspace-write \
  --full-auto \
  "Implement the login feature"
```

**Use for:**
- Implementing new features
- Fixing bugs
- Refactoring code
- Creating new files

### `danger-full-access`

Full system access including network operations. **Use with caution.**

```bash
executables/codex-kkp-cli-{platform} \
  --cd="/path/to/project" \
  --sandbox=danger-full-access \
  "Install dependencies and run tests"
```

**Use for:**
- Installing packages
- Running tests
- Network operations
- System commands

## Security Considerations

| Mode                 | File Read      | File Write     | Network | System Commands |
|----------------------|----------------|----------------|---------|-----------------|
| `read-only`          | Workspace only | No             | No      | No              |
| `workspace-write`    | Workspace only | Workspace only | No      | No              |
| `danger-full-access` | Full           | Full           | Yes     | Yes             |

## Recommendations

1. **Start with `read-only`** - Use for initial analysis and understanding
2. **Escalate to `workspace-write`** - When you need file modifications
3. **Use `danger-full-access` sparingly** - Only when network or system access is required
4. **Combine with `--full-auto`** - Use with `workspace-write` for automated editing
