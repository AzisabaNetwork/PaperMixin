package net.azisaba.papermixin;

import com.google.common.io.ByteStreams;
import net.azisaba.papermixin.helper.EnvProvider;
import net.azisaba.papermixin.helper.IPaperMixinEnvironment;
import net.blueberrymc.nativeutil.NativeUtil;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.Config;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class PaperMixinEnvironment implements IPaperMixinEnvironment {
    private IMixinTransformer transformer;
    public final Set<String> classes = new HashSet<>();
    private boolean initializedMixin = false;

    private final ThreadLocal<Boolean> retransforming = ThreadLocal.withInitial(() -> false);

    public PaperMixinEnvironment(@NotNull PaperMixin plugin) throws IOException {
        EnvProvider.set(this);
        initMixin(plugin);
    }

    public void initMixin(@NotNull PaperMixin plugin) throws IOException {
        if (initializedMixin) throw new RuntimeException("Mixin is already initialized");
        MixinBootstrap.init();
        try {
            // preload classes to prevent errors at runtime
            Class.forName("org.spongepowered.asm.mixin.transformer.MixinInfo");
            Class.forName("org.spongepowered.asm.mixin.Mixins");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        addHook(plugin);
        transformAllClasses(plugin);
        initializedMixin = true;
    }

    public void addHook(@NotNull PaperMixin plugin) {
        NativeUtil.registerClassLoadHook((loader, className, classBeingRedefined, protectionDomain, buf) -> {
            classes.add(className);
            //plugin.getSLF4JLogger().info("Class load hook called for class {} (retransforming: {}, is null: {}, transformer: {})", className, retransforming, buf == null, transformer != null);
            if (buf == null) return null;
            return processClass(className, buf);
        });
    }

    public void transformAllClasses(@NotNull PaperMixin plugin) throws IOException {
        Set<String> classes = new HashSet<>();
        for (Config config : Mixins.getConfigs()) {
            Set<String> set = null;
            try {
                Method method = config.getConfig().getClass().getDeclaredMethod("getUnhandledTargets");
                method.setAccessible(true);
                set = (Set<String>) method.invoke(config.getConfig());
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            PaperMixin.getInstance().getSLF4JLogger().info("Targets of {}: {}", config.getName(), set);
            classes.addAll(set);
        }
        for (Class<?> clazz : NativeUtil.getLoadedClasses()) {
            if (!classes.contains(clazz.getName())) continue;
            if (clazz.getClassLoader() == null || clazz.isArray() || clazz.isPrimitive()) {
                continue;
            }
            if (!clazz.getName().startsWith("net.minecraft.") && !clazz.getName().startsWith("org.bukkit.") && !clazz.getName().startsWith("com.destroystokyo.paper.") && !clazz.getName().startsWith("io.papermc.paper.")) {
                continue;
            }
            byte[] classBytes = getClassBytes(clazz.getClassLoader(), clazz.getName());
            if (classBytes == null) {
                plugin.getSLF4JLogger().warn("Failed to find class bytes for {}", clazz.getName());
                continue;
            }
            plugin.getSLF4JLogger().debug("(Re-)Transforming class {}", clazz.getName());
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
    public byte[] getClassBytes(@NotNull ClassLoader loader, @NotNull String name) {
        try (InputStream stream = loader.getResourceAsStream(name.replace('.', '/') + ".class")) {
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
        transformer = getMixinTransformer();
    }

    @Override
    public @NonNull Set<String> getCachedClasses() {
        return classes;
    }

    @Override
    public @NonNull ClassLoader getBukkitClassLoader() {
        return Bukkit.class.getClassLoader();
    }
}
