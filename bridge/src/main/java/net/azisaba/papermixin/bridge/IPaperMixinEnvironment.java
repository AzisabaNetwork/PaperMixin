package net.azisaba.papermixin.bridge;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.logging.ILogger;

import java.util.Set;

public interface IPaperMixinEnvironment {
    void startTransforming();

    @NotNull
    Set<String> getCachedClasses();

    @Nullable
    ClassLoader getMinecraftClassLoader();

    byte[] getClassBytes(@Nullable ClassLoader loader, @NotNull String name);

    @NotNull
    ILogger getLogger();

    @NotNull
    Set<@NotNull MixinConfigData> getLoadedConfigs();
}
