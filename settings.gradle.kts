plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "codex-cc-skill-service"

include(":codex-kkp-cli")
include(":codex-kkp-sdk")
include(":commons:codex-kkp-common-command")
