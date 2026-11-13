package net.azisaba.papermixin.bridge;

public class EnvProvider {
    private static IPaperMixinEnvironment env;

    public static IPaperMixinEnvironment get() {
        return env;
    }

    public static void set(IPaperMixinEnvironment env) {
        EnvProvider.env = env;
    }
}
