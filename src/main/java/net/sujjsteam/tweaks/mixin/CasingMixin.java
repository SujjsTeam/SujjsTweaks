package net.sujjsteam.tweaks.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.sujjsteam.tweaks.compat.scguns.AmmoBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ribs.scguns.event.GunEventBus;

@Mixin(value = GunEventBus.class, remap = false)
public abstract class CasingMixin {

    @Inject(method = "addCasingToPouch(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At("HEAD"), cancellable = true)
    private static void tweaks$skipCasing(Player player, ItemStack casing, CallbackInfoReturnable<Boolean> cir) {
        if (AmmoBridge.skipCasing(player)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "ejectCasing(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Z)V",
            at = @At("HEAD"), cancellable = true)
    private static void tweaks$hideCasingEffect(Level level, LivingEntity entity, boolean flag, CallbackInfo ci) {
        if (entity instanceof Player player && AmmoBridge.skipCasing(player)) {
            ci.cancel();
        }
    }
}
