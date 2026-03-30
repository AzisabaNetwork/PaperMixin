import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    java
    `java-library`
    id("com.gradleup.shadow") version "9.4.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

group = "net.azisaba"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

val asmVersion = "9.9.1"

configurations.create("include") {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
    compileOnly("org.spongepowered:mixin:0.8.7")
    compileOnly(project(":helper"))
    implementation("net.blueberrymc:native-util:2.2.0")
    compileOnly("org.ow2.asm:asm:$asmVersion")
    compileOnly("org.ow2.asm:asm-util:$asmVersion")
    compileOnly("org.ow2.asm:asm-tree:$asmVersion")
    compileOnly("org.ow2.asm:asm-analysis:$asmVersion")
    compileOnly("org.ow2.asm:asm-commons:$asmVersion")
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    configurations.getByName("include").dependencies.add(project.dependencies.create("org.spongepowered:mixin:0.8.7"))
    configurations.getByName("include").dependencies.add(project.dependencies.create(project(":helper")))
}

tasks {
    test {
        useJUnitPlatform()
    }

    processResources {
        filter<ReplaceTokens>(
            "tokens" to mapOf(
                "version" to project.version.toString(),
            )
        )
    }

    shadowJar {
        into("lib") {
            from(project.configurations.getByName("include"))
        }
    }
}
