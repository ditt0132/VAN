import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
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
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    implementation("org.incendo:cloud-paper:2.0.0-beta.10")
    implementation("org.incendo:cloud-core:2.0.0")
    compileOnly("com.github.GriefPrevention:GriefPrevention:16.18.3")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    compileJava {
        options.encoding = "UTF-8"
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
        dependsOn(shadowJar)
    }
    withType<ShadowJar> {
        dependsOn(build)
//        dependencies {
//            include(dependency("org.incendo:cloud-paper:2.0.0-beta.10"))
//            include(dependency("org.incendo:cloud-core:2.0.0"))
//        }
    }
}

idea {
    module {
        excludeDirs.addAll(listOf(file("run"), file("out"), file(".idea")))
    }
}