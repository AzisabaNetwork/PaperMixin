# PaperMixin

Mixin for Paper-based Minecraft servers!

This is not magic, and you'll need javaagent to properly run it.

## How to use

1. Build the project and add the resulting jar at `build/libs/PaperMixin-x.x.x.jar` to your server's folder
2. Add `-javaagent:path/to/PaperMixin-x.x.x.jar` to your JVM arguments
3. Create mixin module as per `example` directory
4. Add the resulting jar of the mixin module to `<server root>/modules` folder

A lot of hacks are used to make this work, so if it doesn't work, please report it, and I'll try to fix it.

This plugin is not stable (yet). Remember to take a backup of your server data before using this plugin.
