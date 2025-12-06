# Codex KKP

> KKP: **K**otlin Claude Code s**K**ill **P**lugin。

## 概述

Codex KKP 是一个 Claude Code 插件，可与 Codex AI Agent 无缝集成，用于代码分析、实现和协作任务。
提供 Codex AI Agent 的子代理(subagents)和技能(skills)。

## 特性

- **多平台支持**：原生二进制文件支持 macOS、Linux 和 Windows
- **精简结果**：默认情况下，仅返回已完成的结果以减少 token 消耗
- **沙箱模式**：可配置的安全级别（read-only, workspace-write, danger-full-access）
- **会话管理**：恢复之前的会话并保持上下文
- **子代理**：并行运行子代理以提高性能
- **零糖零卡**：非常健康

## 使用方法

安装和配置完成后，您可以在 Claude Code 中使用 Codex KKP 插件。

## 安装

### 从仓库下载

**1. 从仓库下载/克隆源代码**

**1.1 下载源代码**

[下载](https://github.com/ForteScarlet/codex-kkp/archive/refs/heads/plugins/release.zip)
`plugins/release` 分支的源代码并解压。

**1.2 克隆源代码**

**或者**克隆仓库并
[检出 `plugins/release` 分支](https://github.com/ForteScarlet/codex-kkp/tree/plugins/release)。

```bash
git clone -b plugins/release https://github.com/ForteScarlet/codex-kkp.git
```

**2. 添加到 Claude Code 的 marketplace**

运行 claude：

```bash
claude
```

使用 `/plugin`：

```bash
/plugin
```

选择 `Add marketplace`：

```bash
> Add marketplace
```

然后输入您下载/克隆仓库的本地路径。

### 从 releases 下载

前往 [releases](https://github.com/ForteScarlet/codex-kkp/releases)
并选择一个版本（例如[最新版本](https://github.com/ForteScarlet/codex-kkp/releases/latest)）。

从 assets 中选择您需要的内容，下载并配置：

#### 1. 下载 marketplace zip 文件

Assets 中的压缩文件 `codex-agent-collaboration-marketplace.zip` 是包含 plugins 和 skills 在内的完整的 marketplace 包。

下载并解压后，运行 Claude Code：

```bash
claude
```

使用 `/plugin` 命令：

```bash
/plugin
```

选择 `Add marketplace`：

```bash
> Add marketplace
```

然后输入您解压后的 marketplace 目录的本地路径（即包含 `.claude-plugin` 目录的目录）。

#### 2. 下载 skills zip 文件

Assets 中的压缩文件 `codex-agent-collaboration.zip` 是仅包含统一的多平台执行文件的 skill 包（不包括 plugins 等）。
您可以下载并解压，然后将解压后的目录放置在您的 skills 目录中
（例如 `<PROJECT_DIR>/.claude/skills/codex-agent-collaboration/`）。

这个单一的 ZIP 文件包含所有支持平台的可执行文件：

- macOS x86_64 (Intel) - `executables/codex-kkp-cli-macosx64`
- macOS ARM64 (Apple Silicon) - `executables/codex-kkp-cli-macosarm64`
- Linux x86_64 - `executables/codex-kkp-cli-linuxx64`
- Linux ARM64 - `executables/codex-kkp-cli-linuxarm64`
- Windows x86_64 - `executables/codex-kkp-cli-mingwx64`

Claude Code 将自动为您的系统选择适当的可执行文件。

#### 3. 下载可执行二进制文件

Assets 中的独立二进制文件（例如 `codex-kkp-cli-macosx64`、`codex-kkp-cli-mingwx64`）
是每个平台的原始可执行二进制文件。
您可以仅下载适用于您平台的可执行文件并自行设计技能。

注意：所有可执行文件使用统一的命名方式，因此不带文件扩展名（包括 Windows (`mingwx64`) 平台）。

### 工具安装和管理

一些开源项目可能允许通过仓库地址和分支独立管理 marketplace
和技能（例如 [cc-switch](https://github.com/farion1231/cc-switch)，不过它仅限技能）。
您也可以使用这些工具直接基于仓库和分支配置技能。

## 配置

完成插件安装后，
您可以提前配置一些设置。

编辑项目中的 `.claude/settings.local.json` 文件，
并将以下配置添加到 `permissions.allow` 属性中：

**仅安装技能**

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

**通过 marketplace 安装完整插件**

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

> 如果本地加载，则 `~/.claude/marketplace/` 应替换为您本地下载的
`codex-kkp-skills-marketplace` 所在的目录。

在 `Bash` 中，路径指的是已安装插件包内对应您系统平台的可执行文件。
您可能需要根据实际情况进行适当调整。

## 卸载

有些时候，当你尝试更新 marketplace、卸载 marketplace 的时候，由于 Claude Code CLI 本身的问题，可能会导致 plugin 卸载不完全，
进一步导致无法彻底清除 plugin 的启用、重新加载/安装 marketplace 无法启用/重新启用插件等问题。如果遇到此问题，你可以前往
Claude Code 的配置目录中的 `plugins` 子目录中，例如：

```
~/.claude/plugins
```

手动清理 `installed_plugins.json` 、 `known_marketplaces.json` 等配置文件中你已经卸载的 marketplace 和 plugins, 
然后重新开启一个新的 CLI 。

## 许可证

本项目采用 MIT 许可证 - 详情请参阅 [LICENSE](LICENSE) 文件。
