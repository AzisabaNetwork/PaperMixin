package net.azisaba.papermixin.service;

import net.azisaba.papermixin.bridge.EnvProvider;
import org.spongepowered.asm.launch.platform.MainAttributes;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment.CompatibilityLevel;
import org.spongepowered.asm.service.*;
import org.spongepowered.asm.util.Constants;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Mixin service for Paper
 */
public class MixinServicePaper extends MixinServiceAbstract {
    private final PaperClassTracker classTracker = new PaperClassTracker();
    private final PaperClassProvider classProvider = new PaperClassProvider();
    private final PaperClassBytecodeProvider classBytecodeProvider = new PaperClassBytecodeProvider();
    
    @Override
    public String getName() {
        return "Paper";
    }

    @Override
    public boolean isValid() {
        try {
            EnvProvider.get();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public CompatibilityLevel getMaxCompatibilityLevel() {
        return CompatibilityLevel.JAVA_21;
    }
    
    @Override
    protected ILogger createLogger(String name) {
        return EnvProvider.get().getLogger();
    }

    @Override
    public Collection<String> getPlatformAgents() {
        return Collections.singletonList("net.azisaba.papermixin.platform.MixinPlatformAgentPaper");
    }
    
    @Override
    public IContainerHandle getPrimaryContainer() {
        URI uri;
        try {
            uri = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            return new ContainerHandleURI(uri);
        } catch (URISyntaxException ex) {
            EnvProvider.get().getLogger().error("Failed to get primary container", ex);
        }
        return new ContainerHandleVirtual(this.getName());
    }

    @Override
    public Collection<IContainerHandle> getMixinContainers() {
        List<IContainerHandle> list = new ArrayList<>();
        File[] files = new File("modules").listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".jar")) {
                    URI uri = file.toURI();
                    MainAttributes attributes = MainAttributes.of(uri);
                    String tweaker = attributes.get(Constants.ManifestAttributes.MIXINCONFIGS);
                    if (tweaker != null) {
                        list.add(new ContainerHandleURI(uri));
                    }
                }
            }
        }
        return list;
    }

    @Override
    public IClassProvider getClassProvider() {
        return classProvider;
    }

    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
        return classBytecodeProvider;
    }

    @Override
    public ITransformerProvider getTransformerProvider() {
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IMixinService#getClassTracker()
     */
    @Override
    public IClassTracker getClassTracker() {
        return classTracker;
    }
    
    /* (non-Javadoc)
     * @see org.spongepowered.asm.service.IMixinService#getAuditTrail()
     */
    @Override
    public IMixinAuditTrail getAuditTrail() {
        return null;
    }

    @Override
    public void beginPhase() {
        EnvProvider.get().startTransforming();
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        System.out.println("Loading resource: " + name + " from " + this.getClass().getClassLoader());
        return this.getClass().getClassLoader().getResourceAsStream(name);
    }
}
