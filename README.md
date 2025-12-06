# Codex KKP

> KKP: **K**otlin Claude Code s**K**ill **P**lugin.

## Overview

Codex KKP is a Claude Code Plugin that enables seamless integration with Codex AI Agent for code
analysis, implementation, and collaboration tasks. Provides a subagent and a skill for Codex AI Agent.

## Features

- **Multi-platform Support**: Native binaries for macOS, Linux, and Windows
- **Streamlined results**: By default, only completed results are returned to reduce token consumption.
- **Sandbox Modes**: Configurable security levels (read-only, workspace-write, danger-full-access)
- **Session Management**: Resume previous sessions and maintain context
- **Claude Code Subagents**: Run subagents in parallel to improve performance
- **Sugar-Free, Calorie-Free**: Very healthy

## Usage

After installation and configuration, you can use the Codex KKP plugin within Claude Code. The plugin provides 
a subagent and a skill for code analysis, implementation, and collaboration tasks.

## Installation

### Download from repository

**1. Download/Clone source from repository**

**1.1 Download source**

[Download](https://github.com/ForteScarlet/codex-kkp/archive/refs/heads/plugins/release.zip)
the source code for Branch `plugins/release` and extract it.

**1.2 Clone source**

**OR** clone the repository and
[check out Branch `plugins/release`](https://github.com/ForteScarlet/codex-kkp/tree/plugins/release) .

```
git clone -b plugins/release https://github.com/ForteScarlet/codex-kkp.git
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

Choose `Add marketplace`:

```
> Add marketplace
```

and enter the local path where you downloaded/cloned the repository.

### Download from releases

Go to the [releases](https://github.com/ForteScarlet/codex-kkp/releases)
and select a version (such as the [latest](https://github.com/ForteScarlet/codex-kkp/releases/latest) version).

Select the content you need from assets, download it, and configure it:

#### 1. Download marketplace zip file

The compressed file `codex-agent-collaboration-marketplace.zip` in Assets is a complete marketplace package that includes plugins and skills.

After downloading and extracting, run Claude Code:

```bash
claude
```

Use the `/plugin` command:

```bash
/plugin
```

Choose `Add marketplace`:

```bash
> Add marketplace
```

Then enter the local path of your extracted marketplace directory (the directory containing the `.claude-plugin` directory).

#### 2. Download skills zip file

The compressed file `codex-agent-collaboration.zip` in Assets contains a unified multi-platform skill bundle.
You can download and unzip it, then place the extracted directory in your skills directory
(e.g., `<PROJECT_DIR>/.claude/skills/codex-agent-collaboration/`).

This single ZIP file includes executables for all supported platforms:

- macOS x86_64 (Intel) - `executables/codex-kkp-cli-macosx64`
- macOS ARM64 (Apple Silicon) - `executables/codex-kkp-cli-macosarm64`
- Linux x86_64 - `executables/codex-kkp-cli-linuxx64`
- Linux ARM64 - `executables/codex-kkp-cli-linuxarm64`
- Windows x86_64 - `executables/codex-kkp-cli-mingwx64`

Claude Code will automatically select the appropriate executable for your system.

#### 3. Download executable binary file

Standalone binary files (e.g., `codex-kkp-cli-macosx64`, `codex-kkp-cli-mingwx64`)
in Assets are raw executable binaries for each platform.
You can download only the executable file for your platform and design the skills yourself.

Note: All executables use uniform naming without file extensions (including Windows (`mingwx64`) builds).

### Tool installation and management

Some open-source projects may allow independent management of marketplaces
and skills through repository addresses and branches (e.g., [cc-switch](https://github.com/farion1231/cc-switch), skills
only).
You can also configure skills directly based on repositories and branches using these tools.

## Configuration

Once you've finished installing the plugin,
you can proceed to configure some of your settings in advance.

Edit the `.claude/settings.local.json` file in your project
and add the following configuration to property `permissions.allow`:

**Install skill only**

```json
{
  "permissions": {
    "allow": [
      "Skill(codex-agent-collaboration-plugin:codex-agent-collaboration)",
      "Bash(~/.claude/skills/codex-agent-collaboration/executables/codex-kkp-cli-macosx64:*)"
    ]
  }
}
```

**Install full plugin via marketplace**

```json
{
  "permissions": {
    "allow": [
      "Skill(codex-agent-collaboration-plugin:codex-agent-collaboration)",
      "Bash(~/.claude/marketplace/codex-kkp-skills-marketplace/codex-agent-collaboration-plugin/skills/codex-agent-collaboration/executables/codex-kkp-cli-macosx64:*)"
    ]
  }
}
```

> If loading locally, then `~/.claude/marketplace/` should be replaced with the directory of your locally downloaded
`codex-kkp-skills-marketplace`.

In `Bash`, the path refers to the executable file within the installed plugin package
that corresponds to your system platform.
You may need to make slight adjustments based on the actual situation.

## Uninstall

Sometimes, when you try to update or uninstall a marketplace, due to issues with the Claude Code CLI itself, the plugin
may not be completely uninstalled. This can lead to problems such as being unable to completely clear the plugin's
activation, or being unable to enable/re-enable plugins when reloading/reinstalling the marketplace. If you encounter
this issue, you can go to the `plugins` subdirectory in Claude Code's configuration directory, for example:

```
~/.claude/plugins
```

Manually clean up the configuration files like `installed_plugins.json` and `known_marketplaces.json` to remove the
marketplaces and plugins you have already uninstalled, then restart a new CLI.

For this reason, it is recommended to uninstall any plugins before removing marketplace.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.