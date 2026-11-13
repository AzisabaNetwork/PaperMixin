plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

group = "net.azisaba"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
    compileOnly(project(":bridge"))
    compileOnly(project(":helper"))
    compileOnly("net.blueberrymc:native-util:2.2.0")
    compileOnly("org.spongepowered:mixin:0.8.7")
    compileOnly("org.jetbrains:annotations:26.1.0")
}
