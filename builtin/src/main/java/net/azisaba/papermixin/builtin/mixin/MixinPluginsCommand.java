package net.azisaba.papermixin.builtin.mixin;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.PaperPluginsCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.azisaba.papermixin.bridge.EnvProvider;
import net.azisaba.papermixin.bridge.IPaperMixinEnvironment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PaperPluginsCommand.class)
public class MixinPluginsCommand {
    @Shadow
    @Final
    private static TextColor INFO_COLOR;
    @Shadow
    @Final
    private static Component INFO_ICON_START;

    @Shadow
    private static Component asPlainComponents(String strings) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Unique
    private static final Component SERVER_MODULES_INFO =
            Component.text("ℹ What is a server module?", INFO_COLOR)
                    .append(asPlainComponents("Server modules can add or modify the behavior of your server!\n\nLearn more at https://github.com/AzisabaNetwork/PaperMixin"));
    @Unique
    private static final Component INFO_ICON_SERVER_PLUGIN =
            INFO_ICON_START.hoverEvent(SERVER_MODULES_INFO).clickEvent(ClickEvent.openUrl("https://github.com/AzisabaNetwork/PaperMixin"));

    @Inject(at = @At("TAIL"), method = "execute")
    public void sendServerModules(CommandContext<CommandSourceStack> context, CallbackInfoReturnable<Integer> cir) {
        IPaperMixinEnvironment env = EnvProvider.get();
        CommandSender sender = context.getSource().getSender();
        Component infoMessage = Component.text().append(INFO_ICON_SERVER_PLUGIN).append(Component.text("Server Modules (%s):".formatted(env.getLoadedConfigs().size()), NamedTextColor.LIGHT_PURPLE)).build();
        sender.sendMessage(infoMessage);
        sender.sendMessage(Component.text("  - ", NamedTextColor.DARK_GRAY).append(Component.join(JoinConfiguration.commas(true), paperMixin$getServerModules(env))));
    }

    @Unique
    private List<TextComponent> paperMixin$getServerModules(IPaperMixinEnvironment env) {
        return env.getLoadedConfigs().stream().map(config -> {
            TextColor color = config.isVisited() ? NamedTextColor.GREEN : NamedTextColor.RED;
            return Component.text(config.name(), color)
                    .hoverEvent(Component.text("Source: " + config.source()).append(asPlainComponents("Mixin targets:\n- " + String.join("\n- ", config.targets()))));
        }).toList();
    }
}
