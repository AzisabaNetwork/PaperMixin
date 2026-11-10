package net.azisaba.papermixin.service;

import net.azisaba.papermixin.helper.EnvProvider;
import net.blueberrymc.nativeutil.NativeUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.service.IClassProvider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class PaperClassProvider implements IClassProvider {
    public static Throwable lastError;

    @Contract(value = "null -> new", pure = true)
    @NotNull
    private static URL@NotNull[] tryGetURLsFromAppClassLoader(@Nullable ClassLoader cl) {
        // ton of hacks to get the classpath from the AppClassLoader
        if (cl == null) {
            lastError = new RuntimeException("cl is null");
            return new URL[0];
        }
        if (cl instanceof URLClassLoader ucl) {
            return ucl.getURLs();
        }
        Field field;
        try {
            field = cl.getClass().getDeclaredField("ucp");
        } catch (NoSuchFieldException e) {
            try {
                field = cl.getClass().getSuperclass().getDeclaredField("ucp");
            } catch (NoSuchFieldException | NullPointerException ex) {
                ex.addSuppressed(e);
                lastError = ex;
                return new URL[0];
            }
        }
        Object ucp = NativeUtil.getObject(field, cl);
        if (ucp == null) {
            lastError = new RuntimeException("ucp is null");
            return new URL[0];
        }
        Method getURLs;
        try {
            getURLs = ucp.getClass().getMethod("getURLs");
        } catch (NoSuchMethodException e) {
            lastError = e;
            return new URL[0];
        }
        Object urls = NativeUtil.invokeObject(getURLs, ucp);
        if (urls == null) {
            lastError = new RuntimeException("getURLs() is null");
            return new URL[0];
        }
        return (URL[]) urls;
    }

    @Override
    public URL[] getClassPath() {
        return tryGetURLsFromAppClassLoader(EnvProvider.get().getMinecraftClassLoader());
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return Class.forName(name, true, Thread.currentThread().getContextClassLoader());
    }

    @Override
    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, Thread.currentThread().getContextClassLoader());
    }

    @Override
    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, EnvProvider.get().getMinecraftClassLoader());
    }
}
