plugins {
    `java`
    `maven-publish`
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group="fr.formiko.worldpopulatorh"
version="1.1.0"
description="Populate a minecraft world by anning new features and structures"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.aikar.co/content/groups/aikar/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("com.github.HydrolienF:WorldSelectorH:1.4.1")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    processResources {
        val props = mapOf(
            "name" to project.name,
            "version" to project.version,
            "description" to project.description,
            "apiVersion" to "1.20",
            "group" to project.group
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
    runServer {
        downloadPlugins {
            github("HydrolienF", "WorldSelectorH", "1.4.3", "WorldSelectorH-1.4.3.jar")
        }
        minecraftVersion("1.21.1")
    }
}