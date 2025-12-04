package love.forte.tools.codexkkp.sdk

/**
 * Codex 沙箱模式
 *
 * 控制 Codex 的权限级别。默认情况下 Codex 在只读沙箱中运行。
 *
 * ```
 * export type SandboxMode = "read-only" | "workspace-write" | "danger-full-access";
 * ```
 */
enum class SandboxMode(val value: String) {

    READ_ONLY("read-only"),

    WORKSPACE_WRITE("workspace-write"),

    /**
     * 完全访问模式
     *
     * 允许 Codex 编辑文件和执行网络命令。对应 `--sandbox=danger-full-access`
     */
    DANGER_FULL_ACCESS("danger-full-access");

    override fun toString(): String = value
}
