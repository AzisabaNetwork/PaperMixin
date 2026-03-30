package net.azisaba.papermixin;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PaperMixin extends JavaPlugin {
    @Override
    public void onEnable() {
        MixinLoader.load(this);
        initEnv();
    }

    public void initEnv() {
        try {
            // Prevent loading of classes until we are ready
            Class.forName("net.azisaba.papermixin.PaperMixinEnvironment")
                    .getConstructor(PaperMixin.class)
                    .newInstance(this);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to initialize Mixin", e);
        }
    }

    public static @NotNull PaperMixin getInstance() {
        return JavaPlugin.getPlugin(PaperMixin.class);
    }
}
