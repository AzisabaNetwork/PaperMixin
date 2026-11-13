package net.azisaba.papermixin.bridge;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record MixinConfigData(
        String name,
        @NotNull Set<String> targets,
        boolean isVisited,
        String source
) {}
