package net.azisaba.papermixin.service;

import net.azisaba.papermixin.bridge.EnvProvider;
import org.spongepowered.asm.service.IClassTracker;

public class PaperClassTracker implements IClassTracker {
    @Override
    public void registerInvalidClass(String className) {
        // no-op
    }

    @Override
    public boolean isClassLoaded(String className) {
        return EnvProvider.get().getCachedClasses().contains(className);
    }

    @Override
    public String getClassRestrictions(String className) {
        return "";
    }
}
