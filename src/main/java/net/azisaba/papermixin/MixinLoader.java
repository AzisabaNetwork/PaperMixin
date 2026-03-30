package net.azisaba.papermixin;

import net.blueberrymc.nativeutil.NativeUtil;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class MixinLoader {
    public static void load(@NotNull PaperMixin plugin) {
        String asmVersion = "9.9.1";
        addToClasspath(plugin, "lib/asm-" + asmVersion + ".jar");
        addToClasspath(plugin, "lib/asm-analysis-" + asmVersion + ".jar");
        addToClasspath(plugin, "lib/asm-commons-" + asmVersion + ".jar");
        addToClasspath(plugin, "lib/asm-tree-" + asmVersion + ".jar");
        addToClasspath(plugin, "lib/asm-util-" + asmVersion + ".jar");
        addToClasspath(plugin, "lib/mixin-0.8.7.jar");
        addToClasspath(plugin, "lib/helper-1.0.0.jar");
        addToClasspath(plugin, "lib/native-util-2.2.0.jar");
        File[] files = new File("modules").listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".jar")) {
                    try {
                        addToClasspath(plugin, file.toURI().toURL());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private static void addToClasspath(@NotNull PaperMixin plugin, @NotNull String path) {
        try (InputStream stream = plugin.getClass().getClassLoader().getResourceAsStream(path)) {
            if (stream == null) throw new RuntimeException("Failed to find " + path + " in jar");
            Files.createDirectories(plugin.getDataFolder().toPath().resolve("lib"));
            Files.copy(stream, plugin.getDataFolder().toPath().resolve(path), StandardCopyOption.REPLACE_EXISTING);
            URL location = plugin.getDataFolder().toPath().resolve(path).toUri().toURL();
            addToClasspath(plugin, location);
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract " + path + " from jar", e);
        }
    }

    private static void addToClasspath(@NotNull PaperMixin plugin, @NotNull URL location) {
        try {
            plugin.getSLF4JLogger().info("Adding {} to classpath", location);
            ClassLoader loader = Bukkit.class.getClassLoader();
            if (loader instanceof URLClassLoader ucl) {
                Method method = NativeUtil.getMethod(URLClassLoader.class, "addURL", "(Ljava/net/URL;)V");
                NativeUtil.invokeVoid(method, ucl, location);
            } else {
                plugin.getSLF4JLogger().warn("Failed to add {} to classpath: loader is not URLClassLoader (actual type: {})", location, loader.getClass().getName());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to add " + location + " to classpath", e);
        }
    }
}
