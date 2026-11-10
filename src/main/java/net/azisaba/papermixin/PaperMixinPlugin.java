package net.azisaba.papermixin;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class PaperMixinPlugin extends JavaPlugin {
    @Override
    public void onLoad() {
        MixinLoader.load(EnvironmentType.PLUGIN, getSLF4JLogger(), getDataPath());
        initEnv();
    }

    public void initEnv() {
        try {
            // Prevent loading of classes until we are ready
            Class.forName("net.azisaba.papermixin.PaperMixinEnvironment")
                    .getConstructor(EnvironmentType.class, Logger.class)
                    .newInstance(EnvironmentType.PLUGIN, getSLF4JLogger());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to initialize Mixin", e);
        }
    }

    public static @NotNull PaperMixinPlugin getInstance() {
        return JavaPlugin.getPlugin(PaperMixinPlugin.class);
    }
}
