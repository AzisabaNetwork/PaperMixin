plugins {
    id("java")
    id("com.gradleup.shadow") version "9.4.1"
}

group = "net.azisaba"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

val asmVersion = "9.11.1"

dependencies {
    implementation("net.blueberrymc:native-util:2.2.0")
    implementation("org.ow2.asm:asm:${asmVersion}")
    implementation("org.ow2.asm:asm-util:${asmVersion}")
    implementation("org.ow2.asm:asm-tree:${asmVersion}")
    implementation("org.ow2.asm:asm-analysis:${asmVersion}")
    implementation("org.ow2.asm:asm-commons:${asmVersion}")
    compileOnly("org.spongepowered:mixin:0.8.7")
    compileOnly("org.jetbrains:annotations:26.1.0")
}
