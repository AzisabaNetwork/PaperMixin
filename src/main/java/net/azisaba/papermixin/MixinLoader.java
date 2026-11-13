package net.azisaba.papermixin;

import net.blueberrymc.nativeutil.NativeUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

public class MixinLoader {
    public static String applicationClass = "org.bukkit.Bukkit";
    public static Path dataDirectory;

    public static void load(@NotNull EnvironmentType environmentType, @NotNull Logger logger, @NotNull Path dataDirectory) {
        logger.info("Loading PaperMixin version {} (Type={})", MixinLoader.class.getPackage().getImplementationVersion(), environmentType);
        MixinLoader.dataDirectory = dataDirectory;
        String asmVersion = "9.11.1";
        addToClasspath(logger, dataDirectory, "lib/asm-" + asmVersion + ".jar");
        addToClasspath(logger, dataDirectory, "lib/asm-analysis-" + asmVersion + ".jar");
        addToClasspath(logger, dataDirectory, "lib/asm-commons-" + asmVersion + ".jar");
        addToClasspath(logger, dataDirectory, "lib/asm-tree-" + asmVersion + ".jar");
        addToClasspath(logger, dataDirectory, "lib/asm-util-" + asmVersion + ".jar");
        addToClasspath(logger, dataDirectory, "lib/mixin-0.8.7.jar");
        addToClasspath(logger, dataDirectory, "lib/helper-1.0.0.jar");
        addToClasspath(logger, dataDirectory, "lib/native-util-2.2.0.jar");
        if (environmentType == EnvironmentType.AGENT) {
            addToClasspath(logger, dataDirectory, "lib/guava-33.5.0-jre.jar");
            addToClasspath(logger, dataDirectory, "lib/gson-2.13.2.jar");
        }
        forEachModulesDirectory(path -> {
            try {
                addToClasspath(logger, path.toUri().toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void forEachModulesDirectory(@NotNull Consumer<Path> consumer) {
        File[] files = new File("modules").listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".jar")) {
                    consumer.accept(file.toPath());
                }
            }
        }
    }

    private static void addToClasspath(@NotNull Logger logger, @NotNull Path dataDirectory, @NotNull String path) {
        try (InputStream stream = MixinLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) throw new RuntimeException("Failed to find " + path + " in jar");
            Files.createDirectories(dataDirectory.resolve("lib"));
            Files.copy(stream, dataDirectory.resolve(path), StandardCopyOption.REPLACE_EXISTING);
            URL location = dataDirectory.resolve(path).toUri().toURL();
            addToClasspath(logger, location);
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract " + path + " from jar", e);
        }
    }

    public static void addToClasspath(@NotNull Logger logger, @NotNull ClassLoader loader, @NotNull Path dataDirectory, @NotNull String path) {
        try (InputStream stream = MixinLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) throw new RuntimeException("Failed to find " + path + " in jar");
            Files.createDirectories(dataDirectory.resolve("lib"));
            if (!Files.exists(dataDirectory.resolve(path))) {
                Files.copy(stream, dataDirectory.resolve(path), StandardCopyOption.REPLACE_EXISTING);
            }
            URL location = dataDirectory.resolve(path).toUri().toURL();
            addToClasspath(logger, loader, location);
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract " + path + " from jar", e);
        }
    }

    private static void addToClasspath(@NotNull Logger logger, @NotNull URL location) {
        addToClasspath(logger, getClassLoader(), location);
    }

    public static void addToClasspath(@NotNull Logger logger, @NotNull ClassLoader loader, @NotNull URL location) {
        try {
            logger.info("Adding {} to classpath", location);
            if (loader instanceof URLClassLoader ucl) {
                Method method = NativeUtil.getMethod(URLClassLoader.class, "addURL", "(Ljava/net/URL;)V");
                NativeUtil.invokeVoid(method, ucl, location);
            } else if (loader == ClassLoader.getSystemClassLoader()) {
                NativeUtil.appendToSystemClassLoaderSearch(location.toString().substring(5));
            } else {
                logger.warn("Failed to add {} to classpath: loader is not URLClassLoader (actual type: {})", location, loader.getClass().getName());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to add " + location + " to classpath", e);
        }
    }

    private static ClassLoader getClassLoader() {
        try {
            return Class.forName("org.bukkit.Bukkit").getClassLoader();
        } catch (ClassNotFoundException e) {
            return ClassLoader.getSystemClassLoader();
        }
    }
}
