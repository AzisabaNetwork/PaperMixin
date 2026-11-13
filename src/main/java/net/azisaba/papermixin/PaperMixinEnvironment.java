package net.azisaba.papermixin;

import com.google.common.io.ByteStreams;
import net.azisaba.papermixin.helper.EnvProvider;
import net.azisaba.papermixin.helper.IPaperMixinEnvironment;
import net.blueberrymc.nativeutil.NativeUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.launch.MixinBootstrap;
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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PaperMixinEnvironment implements IPaperMixinEnvironment {
    private static final Set<String> MINECRAFT_CLASSES = Set.of("net.minecraft.", "org.bukkit.", "com.destroystokyo.paper.", "io.papermc.paper.", "ca.spottedleaf.");
    private static final Logger LOGGER = LoggerFactory.getLogger(PaperMixinEnvironment.class);
    private static final ILogger LOGGER_ADAPTER = new LoggerAdapterSlf4j("PaperMixin");
    private static boolean initializedMixin = false;
    private static boolean hasInitMinecraft = false;
    private IMixinTransformer transformer;
    private ClassLoader minecraftClassLoader;
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
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        addHook();
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
        MixinLoader.addToClasspath(LOGGER, minecraftClassLoader, MixinLoader.dataDirectory, "lib/mixin-0.8.7.jar");
        MixinLoader.forEachModulesDirectory(path -> {
            try {
                MixinLoader.addToClasspath(LOGGER, minecraftClassLoader, path.toUri().toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void transformAllClasses(@NotNull Logger logger) {
        Set<String> classes = new HashSet<>();
        for (Config config : new HashSet<>(Mixins.getConfigs())) {
            Set<String> set;
            try {
                Method method = config.getConfig().getClass().getDeclaredMethod("getUnhandledTargets");
                method.setAccessible(true);
                set = (Set<String>) method.invoke(config.getConfig());
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            logger.info("Targets of {}: {}", config.getName(), set);
            classes.addAll(set);
        }
        for (Class<?> clazz : NativeUtil.getLoadedClasses()) {
            if (!classes.contains(clazz.getName())) continue;
            if (clazz.getClassLoader() == null || clazz.isArray() || clazz.isPrimitive()) {
                continue;
            }
            if (!isMinecraftClass(clazz.getName())) {
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
        try (InputStream stream = Objects.requireNonNullElseGet(loader, () -> getClass().getClassLoader()).getResourceAsStream(name.replace('.', '/') + ".class")) {
            if (stream == null) return null;
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
