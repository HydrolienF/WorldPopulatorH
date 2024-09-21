plugins {
    `java`
    id("io.papermc.paperweight.userdev") version "1.7.1"
    id("io.github.goooler.shadow") version "8.1.7"
    `maven-publish`
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group="fr.formiko.worldpopulatorh"
version="1.1.5"
description="Populate a minecraft world by anning new features and structures"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.HydrolienF:WorldSelectorH:1.4.1")
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.0.2")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    shadowJar {
        val prefix = "${project.group}.lib"
        sequenceOf(
            "co.aikar",
            "org.bstats",
        ).forEach { pkg ->
            relocate(pkg, "$prefix.$pkg")
        }
        archiveFileName.set("${project.name}-${project.version}.jar")
    }

    assemble {
        dependsOn(shadowJar)
    }

    processResources {
        val props = mapOf(
            "name" to project.name,
            "version" to project.version,
            "description" to project.description,
            "apiVersion" to "1.20",
            "group" to project.group
        )
        inputs.properties(props)
        filesMatching("paper-plugin.yml") {
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