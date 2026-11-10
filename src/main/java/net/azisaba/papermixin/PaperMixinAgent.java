package net.azisaba.papermixin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.nio.file.Paths;

public class PaperMixinAgent {
    static {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(PaperMixinAgent.class);

    public static void premain(String agentArgs, Instrumentation inst) {
        if (agentArgs != null && !agentArgs.isEmpty()) {
            MixinLoader.applicationClass = agentArgs;
        }
        MixinLoader.load(EnvironmentType.AGENT, LOGGER, Paths.get(".papermixin"));
        initEnv();
    }

    public static void initEnv() {
        try {
            // Prevent loading of classes until we are ready
            Class.forName("net.azisaba.papermixin.PaperMixinEnvironment")
                    .getConstructor(EnvironmentType.class, Logger.class)
                    .newInstance(EnvironmentType.AGENT, LOGGER);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to initialize Mixin", e);
        }
    }
}
