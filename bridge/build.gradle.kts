plugins {
    id("java")
}

group = "net.azisaba"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

dependencies {
    compileOnly("net.blueberrymc:native-util:2.2.0")
    compileOnly("org.spongepowered:mixin:0.8.7")
    compileOnly("org.jetbrains:annotations:26.1.0")
}
