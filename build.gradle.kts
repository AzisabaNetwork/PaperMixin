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

val asmVersion = "9.11.1"

configurations.create("include") {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
    compileOnly("org.spongepowered:mixin:0.8.7")
    compileOnly(project(":bridge"))
    compileOnly(project(":helper"))
    implementation("net.blueberrymc:native-util:2.2.0")
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    compileOnly("org.ow2.asm:asm:$asmVersion")
    compileOnly("org.ow2.asm:asm-util:$asmVersion")
    compileOnly("org.ow2.asm:asm-tree:$asmVersion")
    compileOnly("org.ow2.asm:asm-analysis:$asmVersion")
    compileOnly("org.ow2.asm:asm-commons:$asmVersion")
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    configurations.getByName("include").dependencies.add(project.dependencies.create("org.spongepowered:mixin:0.8.7"))
    configurations.getByName("include").dependencies.add(project.dependencies.create("com.google.guava:guava:33.5.0-jre"))
    configurations.getByName("include").dependencies.add(project.dependencies.create("com.google.guava:failureaccess:1.0.3"))
    configurations.getByName("include").dependencies.add(project.dependencies.create("com.google.code.gson:gson:2.13.2"))
    configurations.getByName("include").dependencies.add(project.dependencies.create(project(":helper")))
    configurations.getByName("include").dependencies.add(project.dependencies.create(project(":builtin")))
    configurations.getByName("include").dependencies.add(project.dependencies.create(project(":bridge")))
}

// due to bug in the Java 25
val paperMixinJar by tasks.registering(Jar::class) {
    archiveFileName.set("mixin-0.8.7.jar")
    destinationDirectory.set(layout.buildDirectory.dir("filtered-mixin"))

    from({
        zipTree(configurations.getByName("include").files.single {
            it.name == "mixin-0.8.7.jar"
        })
    })

    exclude(
        "META-INF/services/org.spongepowered.asm.service.IMixinService",
        "META-INF/services/org.spongepowered.asm.service.IMixinServiceBootstrap",
        "META-INF/services/org.spongepowered.asm.service.IGlobalPropertyService",
        "META-INF/*.SF",
        "META-INF/*.RSA",
        "META-INF/*.DSA",
        "META-INF/*.EC",
        "META-INF/SIG-*",
    )
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
        relocate("org.slf4j", "net.azisaba.papermixin.shaded.org.slf4j")

//        into("lib") {
//            from(project.configurations.getByName("include"))
//        }

        into("lib") {
            from(project.configurations.getByName("include")) {
                exclude("mixin-0.8.7.jar")
            }
            from(paperMixinJar.flatMap { it.archiveFile })
        }

        mergeServiceFiles()

        manifest {
            attributes(mapOf(
                "Premain-Class" to "net.azisaba.papermixin.PaperMixinAgent",
                "Can-Redefine-Classes" to "true",
                "Can-Retransform-Classes" to "true",
                "Implementation-Title" to "PaperMixin",
                "Implementation-Version" to project.version.toString(),
                "Implementation-Vendor" to "AzisabaNetwork",
                "Boot-Class-Path" to "lib/helper-1.0.0.jar",
//                "MixinConfigs" to "papermixin.mixins.json", // handled in PaperMixinEnvironment
            ))
        }
    }

    build {
        dependsOn(shadowJar)
    }
}
