package net.azisaba.papermixin;

import com.google.common.io.ByteStreams;
import net.azisaba.papermixin.bridge.EnvProvider;
import net.azisaba.papermixin.bridge.IPaperMixinEnvironment;
import net.azisaba.papermixin.bridge.MixinConfigData;
import net.blueberrymc.nativeutil.NativeUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.Config;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class PaperMixinEnvironment implements IPaperMixinEnvironment {
    private static final Set<String> MINECRAFT_CLASSES = Set.of("net.minecraft.", "org.bukkit.", "com.destroystokyo.paper.", "io.papermc.paper.", "ca.spottedleaf.");
    private static final Logger LOGGER = LoggerFactory.getLogger(PaperMixinEnvironment.class);
    private static final ILogger LOGGER_ADAPTER = new LoggerAdapterSlf4j("PaperMixin");
    private static boolean initializedMixin = false;
    private static boolean hasInitMinecraft = false;
    private IMixinTransformer transformer;
    private ClassLoader minecraftClassLoader;
    public final Set<Config> configs = new HashSet<>();
    public final Set<String> classesToBeTransformed = new HashSet<>();
    public final Set<String> classes = new HashSet<>();

    private final ThreadLocal<Boolean> retransforming = ThreadLocal.withInitial(() -> false);

    public PaperMixinEnvironment(@NotNull EnvironmentType environmentType, @NotNull Logger logger) {
        EnvProvider.set(this);
        initMixin(environmentType, logger);
    }

    public void initMixin(@NotNull EnvironmentType environmentType, @NotNull Logger logger) {
        if (initializedMixin) throw new RuntimeException("Mixin is already initialized");
        MixinBootstrap.init();
        try {
            // preload classes to prevent errors at runtime
            Class.forName("org.spongepowered.asm.mixin.transformer.MixinInfo");
            Class.forName("org.spongepowered.asm.mixin.Mixins");
            Class.forName("org.spongepowered.asm.mixin.injection.callback.CallbackInfo");
            Class.forName("org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable");
            Class.forName("org.spongepowered.asm.mixin.transformer.Config");
            Class.forName("com.google.common.util.concurrent.internal.InternalFutureFailureAccess");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        addHook();
        fetchUnhandledTargets();
        if (environmentType == EnvironmentType.PLUGIN) {
            logger.info("Attempting to transform all classes");
            transformAllClasses(logger);
            logger.info("Transformed all classes");
        }
        initializedMixin = true;
    }

    public void addHook() {
        NativeUtil.registerClassLoadHook((loader, className, classBeingRedefined, protectionDomain, buf) -> {
            classes.add(className);
            if (!hasInitMinecraft && isMinecraftClass(className.replace("/", "."))) {
                minecraftClassLoader = loader;
                initMinecraft();
            }
            if (buf == null) return null;
            return processClass(className, buf);
        });
    }

    public void initMinecraft() {
        hasInitMinecraft = true;
        gotoPhase(MixinEnvironment.Phase.DEFAULT);
        if (MixinLoader.getEnvironmentType() == EnvironmentType.AGENT) {
            MixinLoader.addToClasspath(LOGGER, minecraftClassLoader, MixinLoader.dataDirectory, "lib/mixin-0.8.7.jar");
//            MixinLoader.addToClasspath(LOGGER, minecraftClassLoader, MixinLoader.dataDirectory, "lib/helper-1.0.0.jar");
//            MixinLoader.addToClasspath(LOGGER, minecraftClassLoader, MixinLoader.dataDirectory, "lib/native-util-2.2.0.jar");
//            MixinLoader.addToClasspath(LOGGER, minecraftClassLoader, MixinLoader.getSelfJarUrl());
        }
        MixinLoader.forEachModulesDirectory(path -> {
            try {
                MixinLoader.addToClasspath(LOGGER, minecraftClassLoader, path.toUri().toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void fetchUnhandledTargets() {
        this.configs.clear();
        this.classesToBeTransformed.clear();
        Mixins.addConfigurations(new String[]{"papermixin.mixins.json"}, null);
        this.configs.addAll(Mixins.getConfigs());
        for (Config config : configs) {
            Set<String> set;
            try {
                Method method = config.getConfig().getClass().getDeclaredMethod("getUnhandledTargets");
                method.setAccessible(true);
                set = (Set<String>) method.invoke(config.getConfig());
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            LOGGER.info("Targets of {}: {}", config.getName(), set);
            this.classesToBeTransformed.addAll(set);
        }
    }

    public void transformAllClasses(@NotNull Logger logger) {
        for (Class<?> clazz : NativeUtil.getLoadedClasses()) {
            if (!classesToBeTransformed.contains(clazz.getName())) continue;
            if (clazz.getClassLoader() == null || clazz.isArray() || clazz.isPrimitive()) {
                continue;
            }
            byte[] classBytes = getClassBytes(clazz.getClassLoader(), clazz.getName());
            if (classBytes == null) {
                logger.warn("Failed to find class bytes for {}", clazz.getName());
                continue;
            }
            logger.debug("(Re-)Transforming class {}", clazz.getName());
            //NativeUtil.redefineClasses(new ClassDefinition[]{new ClassDefinition(clazz, processClass(classBytes))});
            try {
                retransforming.set(true);
                NativeUtil.retransformClasses(new Class[]{clazz});
            } finally {
                retransforming.set(false);
            }
        }
    }

    @Override
    public byte[] getClassBytes(@Nullable ClassLoader loader, @NotNull String name) {
        if (name.startsWith("net/azisaba/papermixin/bridge/")) {
            try (JarFile jarFile = new JarFile(".papermixin/lib/bridge-1.0.0.jar");
                 InputStream stream = jarFile.getInputStream(jarFile.getJarEntry(name.replace('.', '/') + ".class"))) {
                if (stream == null) {
                    LOGGER.error("Failed to get class bytes for {}", name);
                    return null;
                }
                return ByteStreams.toByteArray(stream);
            } catch (Exception e) {
                LOGGER.error("Failed to get class bytes for {}", name, e);
                throw new RuntimeException(e);
            }
        }
        try (InputStream stream = Objects.requireNonNullElseGet(loader, () -> getClass().getClassLoader()).getResourceAsStream(name.replace('.', '/') + ".class")) {
            if (stream == null) {
                LOGGER.trace("Failed to get class bytes for {}", name);
                return null;
            }
            return ByteStreams.toByteArray(stream);
        } catch (IOException e) {
            return null;
        }
    }

    @Contract("_, null -> null")
    public byte[] processClass(@NotNull String className, byte[] classBytes) {
        if (classBytes == null) return null;
        if (transformer == null) return classBytes;
        try {
            return transformer.transformClassBytes(className.replace("/", "."), className.replace("/", "."), classBytes);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to transform " + className, e);
        }
    }

    @Contract(value = "-> new", pure = true)
    private @NotNull IMixinTransformer getMixinTransformer() {
        try {
            Constructor<?> ctor = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer").getDeclaredConstructor();
            ctor.setAccessible(true);
            return (IMixinTransformer) ctor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void startTransforming() {
        if (transformer == null) {
            transformer = getMixinTransformer();
        }
    }

    @Override
    public @NonNull Set<String> getCachedClasses() {
        return classes;
    }

    @Override
    public @Nullable ClassLoader getMinecraftClassLoader() {
        try {
            return Class.forName(MixinLoader.applicationClass).getClassLoader();
        } catch (ClassNotFoundException e) {
            return minecraftClassLoader;
        }
    }

    @Override
    public @NotNull ILogger getLogger() {
        return LOGGER_ADAPTER;
    }

    @Override
    public @NotNull Set<@NotNull MixinConfigData> getLoadedConfigs() {
        return configs.stream().map(config -> {
            String source = "<unknown>";
            if (config.getConfig().getSource() instanceof ContainerHandleURI handle) {
                source = handle.getDescription();
            } else if (config.getConfig().getSource() instanceof ContainerHandleVirtual handle) {
                source = handle.getDescription();
            }
            return new MixinConfigData(config.getName(), config.getConfig().getTargets(), config.isVisited(), source);
        }).collect(Collectors.toSet());
    }

    public boolean isMinecraftClass(String className) {
        return MINECRAFT_CLASSES.stream().anyMatch(className::startsWith);
    }

    public static void gotoPhase(MixinEnvironment.Phase phase) {
        try {
            Method method = MixinEnvironment.class.getDeclaredMethod("gotoPhase", MixinEnvironment.Phase.class);
            method.setAccessible(true);
            method.invoke(null, phase);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
