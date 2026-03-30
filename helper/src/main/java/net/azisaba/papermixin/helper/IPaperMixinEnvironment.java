package net.azisaba.papermixin.helper;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface IPaperMixinEnvironment {
    void startTransforming();

    @NotNull
    Set<String> getCachedClasses();

    @NotNull
    ClassLoader getBukkitClassLoader();

    byte[] getClassBytes(@NotNull ClassLoader loader, @NotNull String name);
}
