package net.azisaba.papermixin.service;

import net.azisaba.papermixin.bridge.EnvProvider;
import org.spongepowered.asm.service.IMixinServiceBootstrap;
import org.spongepowered.asm.service.ServiceInitialisationException;

public class MixinServicePaperBootstrap implements IMixinServiceBootstrap {
    @Override
    public String getName() {
        return "Paper";
    }

    @Override
    public String getServiceClassName() {
        return "net.azisaba.papermixin.service.MixinServicePaper";
    }

    @Override
    public void bootstrap() {
        try {
            EnvProvider.get();
        } catch (Throwable th) {
            throw new ServiceInitialisationException("PaperMixin is not available?");
        }
    }
}
