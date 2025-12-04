# Codex KKP

> KKP: **K**otlin Claude Code s**K**ill **P**lugin.

## Overview

Codex KKP is a Kotlin-based Claude Code Skills Plugin that enables seamless integration with Codex AI Agent for code
analysis, implementation, and collaboration tasks.

## Features

- **Multi-platform Support**: Native binaries for macOS (Intel/ARM), Linux (x64/ARM64), and Windows
- **Comprehensive CLI**: Full command-line interface with extensive options
- **Sandbox Modes**: Configurable security levels (read-only, workspace-write, danger-full-access)
- **Event Processing**: Structured event output with filtering and JSON support
- **Session Management**: Resume previous sessions and maintain context

## Installation

### Download from repository

**1. Download/Clone source from repository**

**1.1 Download source**

[Download](https://github.com/ForteScarlet/codex-kkp/archive/refs/heads/skills/release.zip) 
the source code for Branch `skills/release` and extract it, 

**1.2 Clone source**

**OR** clone the repository and 
[check out Branch `skills/release`](https://github.com/ForteScarlet/codex-kkp/tree/skills/release) .

```
git clone -b skills/release https://github.com/ForteScarlet/codex-kkp.git
```

**2. Add into Claude Code's marketplace**

Run claude:

```
claude
```

Use `/plugin`:
```
/plugin
```

Choose `Add aarketplace` and enter the local path where you downloaded/cloned the repository.

### Download from releases

Go to the [releases](https://github.com/ForteScarlet/codex-kkp/releases)
page and select a version (such as the [latest](https://github.com/ForteScarlet/codex-kkp/releases/latest) version).

Select the content you need from assets, download it, and configure it.

#### Download skills zip file

The compressed file `codex-agent-collaboration.zip` in Assets contains a unified multi-platform skill bundle.
You can download and unzip it, then place the extracted directory in your skills directory
(e.g., `<PROJECT_DIR>/.claude/skills/codex-agent-collaboration/`).

This single ZIP file includes executables for all supported platforms:
- macOS Intel (x86_64) - `executables/codex-kkp-cli-macosx64`
- macOS Apple Silicon (ARM64) - `executables/codex-kkp-cli-macosarm64`
- Linux x86_64 - `executables/codex-kkp-cli-linuxx64`
- Linux ARM64 - `executables/codex-kkp-cli-linuxarm64`
- Windows x86_64 - `executables/codex-kkp-cli-mingwx64`

Claude Code will automatically select the appropriate executable for your system.

#### Download executable binary file

Standalone binary files (e.g., `codex-kkp-cli-macosx64`, `codex-kkp-cli-mingwx64`)
in Assets are raw executable binaries for each platform.
You can download only the executable file for your platform and design the skills yourself.

Note: All executables use uniform naming without file extensions (including Windows builds).

### Tool installation and management

Some open-source projects may allow independent management of marketplaces 
and skills through repository addresses and branches (e.g., [cc-switch](https://github.com/farion1231/cc-switch)). 
You can also configure skills directly based on repositories and branches using these tools.