import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    kotlin("jvm") version "1.9.21"
    id("xyz.jpenilla.run-paper") version "2.1.0"
    id("com.gradleup.shadow") version "8.3.1"
}

group = "van.van"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("cloud.commandframework:cloud-paper:1.8.4")
    implementation("dev.jorel:commandapi-bukkit-core:9.5.0")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    }
    processResources {
        filesMatching("**/*.yml") {
            expand(project.properties)
        }
    }
    jar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")
        archiveVersion.set("")
    }
    runServer {
        minecraftVersion("1.20.4")
        jvmArgs = listOf("-Dcom.mojang.eula.agree=true")
    }
}

idea {
    module {
        excludeDirs.addAll(listOf(file("run"), file("out"), file(".idea")))
    }
}