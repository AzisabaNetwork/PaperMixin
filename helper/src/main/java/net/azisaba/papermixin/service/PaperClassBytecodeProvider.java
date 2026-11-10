package net.azisaba.papermixin.service;

import net.azisaba.papermixin.helper.EnvProvider;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.transformers.MixinClassReader;

import java.io.IOException;
import java.util.Objects;

public class PaperClassBytecodeProvider implements IClassBytecodeProvider {
    private ClassNode getClassNode(String className, byte[] classBytes, int flags) {
        Objects.requireNonNull(classBytes, "classBytes");
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new MixinClassReader(classBytes, className);
        classReader.accept(classNode, flags);
        return classNode;
    }

    @Override
    public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
        return getClassNode(name, false);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
        return getClassNode(name, runTransformers, ClassReader.EXPAND_FRAMES);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers, int readerFlags) throws ClassNotFoundException, IOException {
        byte[] classBytes = EnvProvider.get().getClassBytes(this.getClass().getClassLoader(), name);
        if (classBytes == null) classBytes = EnvProvider.get().getClassBytes(EnvProvider.get().getMinecraftClassLoader(), name);
        return getClassNode(name, classBytes, readerFlags);
    }
}
