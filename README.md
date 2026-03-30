# PaperMixin

Mixin for Paper-based Minecraft servers!

This is not magic, and you'll need JetBrains Runtime to properly run it.

## How to use

1. Download https://github.com/JetBrains/JetBrainsRuntime and extract it somewhere (If you are using itzg/minecraft-server, you can mount it as a volume on `/opt/java/openjdk`)
2. Add `-XX:+AllowEnhancedClassRedefinition` to your JVM arguments
3. Build the project and add the resulting jar at `build/libs/PaperMixin-x.x.x.jar` to your server's plugins folder
4. Create mixin module as per `example` directory
5. Add the resulting jar of the mixin module to `<server root>/modules` folder

A lot of hacks are used to make this work, so if it doesn't work, please report it, and I'll try to fix it.

## Why JetBrains Runtime?

Other Java Runtime environments do not allow adding or removing methods. However, JetBrains Runtime allows it.
