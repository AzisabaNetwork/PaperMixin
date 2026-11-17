package com.example.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(EnchantmentMenu.class)
public class MixinEnchantmentMenu {
    @Unique
    private static final List<BlockPos> example$EXTRA_BOOKSHELF_OFFSETS = BlockPos.betweenClosedStream(-3, 0, -3, 3, 2, 3)
            .filter(blockPos -> Math.abs(blockPos.getX()) == 3 || Math.abs(blockPos.getZ()) == 3)
            .map(BlockPos::immutable)
            .toList();

    @Inject(method = "lambda$slotsChanged$0", at = @At("HEAD"))
    private void slotsChanged(ItemStack item, Level level, BlockPos pos, CallbackInfo ci, @Share("level") LocalRef<Level> levelRef, @Share("pos") LocalRef<BlockPos> posRef) {
        levelRef.set(level);
        posRef.set(pos);
    }

    @ModifyExpressionValue(method = "lambda$slotsChanged$0", at = @At(value = "CONSTANT", args = "intValue=0", ordinal = 0))
    private int getBookshelfPositions(int i, @Share("level") LocalRef<Level> levelRef, @Share("pos") LocalRef<BlockPos> posRef) {
        for (BlockPos blockPos : example$EXTRA_BOOKSHELF_OFFSETS) {
            if (EnchantingTableBlock.isValidBookShelf(levelRef.get(), posRef.get(), blockPos)) {
                i++;
            }
        }
        return i;
    }
}
