pluginManagement {
    plugins {
        kotlin("jvm") version "2.1.10"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "resent-open-source-projects"

include("eaglercraft:build-plugin")
project(":eaglercraft:build-plugin").projectDir = file("eaglercraft/build-plugin")