package com.example.mixin;

import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginPacketListenerImpl.class)
public class MixinServerLoginPacketListenerImpl {
    @Inject(method = "tickTimeout", at = @At("HEAD"), cancellable = true)
    public void doNotTickTimeout(CallbackInfo ci) {
        ci.cancel();
    }
}
