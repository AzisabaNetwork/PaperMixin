package com.example.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public class MixinEnchantmentHelper {
    @Definition(id = "power", local = @Local(type = int.class, ordinal = 1, argsOnly = true))
    @Expression("power > 15")
    @ModifyExpressionValue(method = "getEnchantmentCost", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean skipMaxEnchantLevel(boolean original) {
        return false;
    }
}
