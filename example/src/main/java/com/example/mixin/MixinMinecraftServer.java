package com.example.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    @Inject(method = "haveTime", at = @At("HEAD"), cancellable = true)
    public void doNotWait(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "recordTaskExecutionTimeWhileWaiting", at = @At("HEAD"), cancellable = true)
    public void doNotRecordTaskExecutionTime(CallbackInfo ci) {
        ci.cancel();
    }
}
