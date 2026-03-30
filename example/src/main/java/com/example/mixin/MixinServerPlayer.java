package com.example.mixin;

import com.mojang.authlib.GameProfile;
import io.papermc.paper.adventure.AdventureComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player {
    @Shadow
    @Final
    private static Logger LOGGER;

    public MixinServerPlayer(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Inject(at = @At("HEAD"), method = "sendSystemMessage(Lnet/minecraft/network/chat/Component;Z)V")
    public void interceptSystemMessage(Component message, boolean overlay, CallbackInfo ci) {
        String str;
        if (message instanceof AdventureComponent adventure) {
            str = PlainTextComponentSerializer.plainText().serialize(adventure.adventure$component());
        } else {
            str = message.getString();
        }
        LOGGER.info("Player {} received message: {}", nameAndId().name(), str);
    }
}
