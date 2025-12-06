plugins {
    kotlin("jvm") version "2.2.21" apply false
    kotlin("multiplatform") version "2.2.21" apply false
    kotlin("plugin.serialization") version "2.2.21" apply false
}

group = "love.forte.tools"
version = "0.0.7"

allprojects {
    repositories {
        mavenCentral()
    }
}
