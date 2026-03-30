package com.example.mixin;

import io.papermc.paper.adventure.AdventureComponent;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.network.chat.Component;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftPlayer.class)
public abstract class MixinCraftPlayer implements Player {
    @Inject(at = @At("HEAD"), method = "sendMessage(Lnet/kyori/adventure/identity/Identity;Lnet/kyori/adventure/text/Component;Lnet/kyori/adventure/audience/MessageType;)V")
    public void interceptSystemMessage(Identity identity, net.kyori.adventure.text.Component message, MessageType type, CallbackInfo ci) {
        String str = PlainTextComponentSerializer.plainText().serialize(message);
        System.out.println("Player " + getName() + " received message: " + str);
    }
}
