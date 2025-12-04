# Usage Examples

This document provides examples of using the Codex CLI for various tasks.

## Basic Examples

### Simple Task

```bash
executables/codex-kkp-cli-{platform} --cd=/path/to/project "Explain the main function in Main.kt"
```

### Continue Previous Session

```bash
executables/codex-kkp-cli-{platform} --cd=/path/to/project --session=previous-session-id "Now implement the suggested changes"
```

### Code Review with Full Auto

```bash
executables/codex-kkp-cli-{platform} --cd=/path/to/project --full-auto --sandbox=workspace-write "Review this implementation for bugs and suggest improvements"
```

## Advanced Examples

### With Image Input

```bash
executables/codex-kkp-cli-{platform} --cd=/path/to/project --image=/path/to/screenshot.png "Implement the UI shown in this design"
```

### Multiple Images

```bash
executables/codex-kkp-cli-{platform} --cd=/path/to/project --image=/path/to/design1.png --image=/path/to/design2.png "Compare these two designs and implement the better approach"
```

### Full Event Output

```bash
executables/codex-kkp-cli-{platform} --cd=/path/to/project --full "Refactor the authentication module"
```

### Save Last Message to File

```bash
executables/codex-kkp-cli-{platform} --cd=/path/to/project --output-last-message=/tmp/codex-response.txt "Generate API documentation"
```

### Structured Output with Schema

```bash
executables/codex-kkp-cli-{platform} --cd=/path/to/project --output-schema=/path/to/schema.json "Extract all function signatures from this module"
```

### With Git Repository Check

By default, Git repository check is skipped (`--skip-git-repo-check=true`).
To enable Git check (e.g., for ensuring clean working directory):

```bash
executables/codex-kkp-cli-{platform} --cd=/path/to/project --skip-git-repo-check=false "Analyze uncommitted changes"
```
